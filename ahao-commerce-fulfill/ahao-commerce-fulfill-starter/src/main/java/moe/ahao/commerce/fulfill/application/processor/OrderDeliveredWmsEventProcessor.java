package moe.ahao.commerce.fulfill.application.processor;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.api.event.OrderEvent;
import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;
import moe.ahao.commerce.fulfill.api.event.OrderDeliveredWmsEvent;
import moe.ahao.commerce.fulfill.api.event.TriggerOrderWmsShipEvent;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.mapper.OrderFulfillMapper;
import moe.ahao.util.commons.io.JSONHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单已配送事件处理器
 */
@Slf4j
@Service
public class OrderDeliveredWmsEventProcessor extends AbstractWmsShipEventProcessor {
    @Autowired
    private OrderFulfillMapper orderFulfillMapper;

    @Override
    protected void doBizProcess(TriggerOrderWmsShipEvent event) {
        OrderDeliveredWmsEvent deliveredWmsEvent = (OrderDeliveredWmsEvent) event.getWmsEvent();
        String fulfillId = event.getFulfillId();
        // 更新配送员信息
        orderFulfillMapper.updateDelivererInfoByFulfillId(fulfillId,
            deliveredWmsEvent.getDelivererNo(),
            deliveredWmsEvent.getDelivererName(), deliveredWmsEvent.getDelivererPhone());
    }

    @Override
    protected String buildMsgBody(TriggerOrderWmsShipEvent event) {
        String orderId = event.getOrderId();
        // 订单已配送事件
        OrderDeliveredWmsEvent deliveredWmsEvent = (OrderDeliveredWmsEvent) event.getWmsEvent();
        deliveredWmsEvent.setOrderId(orderId);

        // 构建订单已配送消息体
        OrderEvent<OrderDeliveredWmsEvent> orderEvent = buildOrderEvent(orderId, OrderStatusChangeEnum.ORDER_DELIVERED,
                deliveredWmsEvent, OrderDeliveredWmsEvent.class);
        return JSONHelper.toString(orderEvent);
    }
}
