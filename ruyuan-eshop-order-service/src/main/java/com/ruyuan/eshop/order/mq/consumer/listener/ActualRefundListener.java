package com.ruyuan.eshop.order.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.message.RefundMessage;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.mq.consumer.AbstractRocketMqListener;
import com.ruyuan.eshop.order.service.OrderAfterSaleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMqConstant.ACTUAL_REFUND_TOPIC,
        consumerGroup = RocketMqConstant.ACTUAL_REFUND_CONSUMER_GROUP,
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        consumeThreadMax = 1
)
public class ActualRefundListener extends AbstractRocketMqListener {

    @Autowired
    private OrderAfterSaleService orderAfterSaleService;

    @Override
    public void onMessage(String message) {
        RefundMessage refundMessage = JSONObject.parseObject(message, RefundMessage.class);
        log.info("ActualRefundConsumer message:{}", message);

        //  执行实际退款
        JsonResult<Boolean> jsonResult = orderAfterSaleService.refundMoney(refundMessage);
        if (!jsonResult.getSuccess()) {
            throw new OrderBizException(jsonResult.getErrorCode(), jsonResult.getErrorMessage());
        }
    }
}