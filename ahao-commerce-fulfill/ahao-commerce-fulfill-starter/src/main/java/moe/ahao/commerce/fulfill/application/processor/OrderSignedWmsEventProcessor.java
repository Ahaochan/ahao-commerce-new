package moe.ahao.commerce.fulfill.application.processor;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.api.event.OrderEvent;
import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;
import moe.ahao.commerce.fulfill.api.event.OrderSignedWmsEvent;
import moe.ahao.commerce.fulfill.api.event.TriggerOrderWmsShipEvent;
import moe.ahao.util.commons.io.JSONHelper;
import org.springframework.stereotype.Service;

/**
 * 订单已签收事件处理器
 */
@Service
@Slf4j
public class OrderSignedWmsEventProcessor extends AbstractWmsShipEventProcessor {

    @Override
    protected void doBizProcess(TriggerOrderWmsShipEvent event) {
        log.info("准备发送【订单已签收事件】");
    }

    @Override
    protected String buildMsgBody(TriggerOrderWmsShipEvent event) {
        String orderId = event.getOrderId();
        // 订单已签收事件
        OrderSignedWmsEvent signedWmsEvent = (OrderSignedWmsEvent) event.getWmsEvent();
        signedWmsEvent.setOrderId(orderId);

        // 构建订单已签收消息体
        OrderEvent<OrderSignedWmsEvent> orderEvent = buildOrderEvent(orderId, OrderStatusChangeEnum.ORDER_SIGNED,
                signedWmsEvent, OrderSignedWmsEvent.class);

        return JSONHelper.toString(orderEvent);
    }
}
