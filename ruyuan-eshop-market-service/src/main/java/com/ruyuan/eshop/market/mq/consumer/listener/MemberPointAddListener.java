package com.ruyuan.eshop.market.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.message.OrderStdChangeEvent;
import com.ruyuan.eshop.common.mq.AbstractMessageListenerConcurrently;
import com.ruyuan.eshop.common.redis.RedisLock;
import com.ruyuan.eshop.market.dao.MemberPointDAO;
import com.ruyuan.eshop.market.dao.MemberPointDetailDAO;
import com.ruyuan.eshop.market.domain.entity.MemberPointDO;
import com.ruyuan.eshop.market.domain.entity.MemberPointDetailDO;
import com.ruyuan.eshop.market.exception.MarketBizException;
import com.ruyuan.eshop.market.exception.MarketErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 会员积分增加监听器
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class MemberPointAddListener extends AbstractMessageListenerConcurrently {

    private static final Double RATE = 0.1;

    @Autowired
    private MemberPointDAO memberPointDAO;

    @Autowired
    private MemberPointDetailDAO memberPointDetailDAO;

    @Autowired
    private RedisLock redisLock;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            for (MessageExt msg : msgs) {
                String message = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.info("会员积分增加监听器:{}", message);
                OrderStdChangeEvent orderStdChangeEvent = JSONObject.parseObject(message, OrderStdChangeEvent.class);
                String userId = orderStdChangeEvent.getUserId();
                Integer payAmount = orderStdChangeEvent.getPayAmount();
                Integer increasedPoint = Double.valueOf(Math.ceil(payAmount * RATE)).intValue();
                OrderStatusChangeEnum statusChange = orderStdChangeEvent.getStatusChange();

                if (OrderStatusChangeEnum.ORDER_PAID.equals(statusChange)
                        || OrderStatusChangeEnum.SUB_ORDER_PAID.equals(statusChange)) {

                    String key = RedisLockKeyConstants.UPDATE_USER_POINT_KEY + userId;
                    if (!redisLock.tryLock(key)) {
                        throw new MarketBizException(MarketErrorCodeEnum.UPDATE_POINT_ERROR);
                    }

                    try {
                        //  1、查询用户的积分
                        MemberPointDO memberPoint = memberPointDAO.getByUserId(userId);
                        if (null == memberPoint) {
                            memberPoint = buildMemberPoint(userId);
                            memberPointDAO.save(memberPoint);
                        }

                        // 2、添加会员积分明细
                        MemberPointDetailDO detailDO = buildAddMemberPointDetail(userId, increasedPoint, memberPoint);
                        memberPointDetailDAO.save(detailDO);

                        // 3、更新会员积分
                        memberPointDAO.addUserPoint(userId, memberPoint.getPoint(), increasedPoint);

                    } finally {
                        redisLock.unlock(key);
                    }
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }


    private MemberPointDO buildMemberPoint(String userId) {
        MemberPointDO memberPointDO = new MemberPointDO();
        memberPointDO.setPoint(0);
        memberPointDO.setUserId(userId);
        return memberPointDO;
    }


    private MemberPointDetailDO buildAddMemberPointDetail(String userId, Integer increasedPoint, MemberPointDO memberPoint) {
        MemberPointDetailDO detailDO = new MemberPointDetailDO();
        detailDO.setMemberPointId(memberPoint.getId());
        detailDO.setUserId(userId);

        detailDO.setOldPoint(memberPoint.getPoint());
        detailDO.setUpdatedPoint(increasedPoint);
        detailDO.setNewPoint(memberPoint.getPoint() + increasedPoint);

        return detailDO;
    }
}
