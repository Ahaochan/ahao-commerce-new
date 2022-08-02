package com.ruyuan.eshop.order.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.request.CancelOrderAssembleRequest;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
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
        topic = RocketMqConstant.RELEASE_ASSETS_TOPIC,
        consumerGroup = RocketMqConstant.REQUEST_CONSUMER_GROUP,
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        consumeThreadMax = 1
)
public class CancelRefundListener extends AbstractRocketMqListener {

    @Autowired
    private OrderAfterSaleService orderAfterSaleService;

    @Override
    public void onMessage(String message) {
        CancelOrderAssembleRequest cancelOrderAssembleRequest = JSONObject.parseObject(message, CancelOrderAssembleRequest.class);
        log.info("取消退款消息监听器收到message:{}", message);

        //  未支付的订单不需要进入取消退款流程,未付款不记售后单
        OrderInfoDTO orderInfoDTO = cancelOrderAssembleRequest.getOrderInfoDTO();
        if (orderInfoDTO.getOrderStatus() <= OrderStatusEnum.CREATED.getCode()) {
            return;
        }

        //  执行取消订单前操作
        JsonResult<Boolean> jsonResult = orderAfterSaleService.processCancelOrder(cancelOrderAssembleRequest);
        if (!jsonResult.getSuccess()) {
            throw new OrderBizException(OrderErrorCodeEnum.CONSUME_MQ_FAILED);
        }
    }
}