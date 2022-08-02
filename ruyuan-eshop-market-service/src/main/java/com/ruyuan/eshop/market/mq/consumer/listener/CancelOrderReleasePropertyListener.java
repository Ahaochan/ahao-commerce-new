package com.ruyuan.eshop.market.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.mq.AbstractMessageListenerConcurrently;
import com.ruyuan.eshop.market.api.MarketApi;
import com.ruyuan.eshop.market.domain.request.ReleaseUserCouponRequest;
import com.ruyuan.eshop.market.exception.MarketBizException;
import com.ruyuan.eshop.market.exception.MarketErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class CancelOrderReleasePropertyListener extends AbstractMessageListenerConcurrently {

    @DubboReference(version = "1.0.0")
    private MarketApi marketApi;

    /**
     * map参数key名
     */
    public final static String ORDER_INFO_DTO = "orderInfoDTO";
    public final static String COUPON_ID = "couponId";
    public final static String USER_ID = "userId";
    public final static String ORDER_ID = "orderId";

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt msg : list) {
                String message = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.info("释放优惠券消息监听器收到message:{}", message);
                Map<String, Object> orderInfoMap = getOrderInfoMap(message);

                //  没有优惠券,不用释放
                if (Strings.isNullOrEmpty(String.valueOf(orderInfoMap.get("couponId")))) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                ReleaseUserCouponRequest releaseUserCouponRequest = buildReleaseUserCoupon(orderInfoMap);
                //  释放优惠券
                JsonResult<Boolean> jsonResult = marketApi.releaseUserCoupon(releaseUserCouponRequest);
                if (!jsonResult.getSuccess()) {
                    throw new MarketBizException(MarketErrorCodeEnum.CONSUME_MQ_FAILED);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrderInfoMap(String message) {
        Map<String, Object> paramMap = JSONObject.parseObject(message, Map.class);
        String orderInfoStr = paramMap.get(ORDER_INFO_DTO).toString();
        return JSONObject.parseObject(orderInfoStr, Map.class);
    }

    /**
     * 组装释放优惠券数据
     */
    private ReleaseUserCouponRequest buildReleaseUserCoupon(Map<String, Object> orderInfoMap) {
        ReleaseUserCouponRequest releaseUserCouponRequest = new ReleaseUserCouponRequest();
        releaseUserCouponRequest.setCouponId(String.valueOf(orderInfoMap.get(COUPON_ID)));
        releaseUserCouponRequest.setUserId(String.valueOf(orderInfoMap.get(USER_ID)));
        releaseUserCouponRequest.setOrderId(String.valueOf(orderInfoMap.get(ORDER_ID)));
        return releaseUserCouponRequest;
    }
}