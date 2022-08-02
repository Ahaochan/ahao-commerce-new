package com.ruyuan.eshop.order.statemachine.action.order.create.node;

import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.request.OrderPaymentDelayRequest;
import com.ruyuan.eshop.order.service.RocketMqService;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class CreateOrderSendPayTimeoutDelayMessageNode extends StandardProcessor {

    @Autowired
    private RocketMqService rocketMqService;

    @Override
    protected void processInternal(ProcessContext processContext) {
        OrderInfoDTO orderInfoDTO = processContext.get("orderInfoDTO");
        OrderPaymentDelayRequest orderPaymentDelayRequest = new OrderPaymentDelayRequest();
        orderPaymentDelayRequest.setOrderId(orderInfoDTO.getOrderId());

        rocketMqService.sendOrderPayTimeoutDelayMessage(orderPaymentDelayRequest);
    }
}
