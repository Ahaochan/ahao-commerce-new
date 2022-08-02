package com.ruyuan.eshop.order.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.order.mq.consumer.AbstractRocketMqListener;
import com.ruyuan.eshop.order.service.OrderAfterSaleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听 支付订单超时延迟消息
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMqConstant.PAY_ORDER_TIMEOUT_DELAY_TOPIC,
        consumerGroup = RocketMqConstant.PAY_ORDER_TIMEOUT_DELAY_CONSUMER_GROUP,
        selectorExpression = "*",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        consumeThreadMax = 1
)
public class PayOrderTimeoutListener extends AbstractRocketMqListener {

    @Autowired
    private OrderAfterSaleService orderAfterSaleService;

    @Override
    public void onMessage(String message) {
        JSONObject object = JSONObject.parseObject(message);
        String orderId = object.getString("orderId");
        //  执行取消订单前验证
        orderAfterSaleService.verifyBeforeOrderCancellation(orderId);
    }
}