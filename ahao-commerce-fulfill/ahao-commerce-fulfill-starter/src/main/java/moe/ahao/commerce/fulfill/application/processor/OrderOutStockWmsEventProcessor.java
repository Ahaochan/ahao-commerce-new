package moe.ahao.commerce.fulfill.application.processor;


import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.api.event.OrderEvent;
import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;
import moe.ahao.commerce.fulfill.api.event.OrderOutStockWmsEvent;
import moe.ahao.commerce.fulfill.api.event.TriggerOrderWmsShipEvent;
import moe.ahao.util.commons.io.JSONHelper;
import org.springframework.stereotype.Service;

/**
 * 订单已出库事件处理器
 */
@Service
@Slf4j
public class OrderOutStockWmsEventProcessor extends AbstractWmsShipEventProcessor {

    @Override
    protected void doBizProcess(TriggerOrderWmsShipEvent event) {
        log.info("准备发送【订单已出库事件】");
    }

    @Override
    protected String buildMsgBody(TriggerOrderWmsShipEvent event) {
        String orderId = event.getOrderId();
        // 订单已出库事件
        OrderOutStockWmsEvent outStockEvent = (OrderOutStockWmsEvent) event.getWmsEvent();
        outStockEvent.setOrderId(orderId);

        // 构建订单已出库消息体
        OrderEvent<OrderOutStockWmsEvent> orderEvent = buildOrderEvent(orderId, OrderStatusChangeEnum.ORDER_OUT_STOCKED,
                outStockEvent, OrderOutStockWmsEvent.class);

        return JSONHelper.toString(orderEvent);
    }
}
