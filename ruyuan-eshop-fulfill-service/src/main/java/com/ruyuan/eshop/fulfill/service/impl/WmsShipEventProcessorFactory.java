package com.ruyuan.eshop.fulfill.service.impl;

import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.fulfill.service.OrderAfterFulfillEventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class WmsShipEventProcessorFactory {

    @Autowired
    private OrderOutStockEventProcessor orderOutStockEventProcessor;

    @Autowired
    private OrderDeliveredEventProcessor orderDeliveredEventProcessor;

    @Autowired
    private OrderSignedEventProcessor orderSignedEventProcessor;

    /**
     * 订单物流配送结果处理器
     */
    public OrderAfterFulfillEventProcessor getWmsShipEventProcessor(OrderStatusChangeEnum orderStatusChange) {
        if (OrderStatusChangeEnum.ORDER_OUT_STOCKED.equals(orderStatusChange)) {
            //订单已出库事件
            return orderOutStockEventProcessor;
        } else if (OrderStatusChangeEnum.ORDER_DELIVERED.equals(orderStatusChange)) {
            //订单已配送事件
            return orderDeliveredEventProcessor;
        } else if (OrderStatusChangeEnum.ORDER_SIGNED.equals(orderStatusChange)) {
            //订单已签收事件
            return orderSignedEventProcessor;
        }
        return orderOutStockEventProcessor;
    }

}
