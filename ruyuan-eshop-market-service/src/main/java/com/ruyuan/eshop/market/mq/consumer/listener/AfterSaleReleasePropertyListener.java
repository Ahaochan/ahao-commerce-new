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
public class AfterSaleReleasePropertyListener extends AbstractMessageListenerConcurrently {

    @DubboReference(version = "1.0.0")
    private MarketApi marketApi;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt msg : list) {
                String message = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.info("释放优惠券消息监听器收到message:{}", message);
                Map<String, Object> paramMap = JSONObject.parseObject(message, Map.class);
                //  没有优惠券,不用释放
                String couponId = String.valueOf(paramMap.get("couponId"));
                if (Strings.isNullOrEmpty(couponId)) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String userId = String.valueOf(paramMap.get("userId"));
                String orderId = String.valueOf(paramMap.get("orderId"));
                ReleaseUserCouponRequest releaseUserCouponRequest = buildReleaseUserCoupon(couponId, userId, orderId);
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

    /**
     * 组装释放优惠券数据
     */
    private ReleaseUserCouponRequest buildReleaseUserCoupon(String couponId, String userId, String orderId) {
        ReleaseUserCouponRequest releaseUserCouponRequest = new ReleaseUserCouponRequest();
        releaseUserCouponRequest.setCouponId(couponId);
        releaseUserCouponRequest.setUserId(userId);
        releaseUserCouponRequest.setOrderId(orderId);
        return releaseUserCouponRequest;
    }
}