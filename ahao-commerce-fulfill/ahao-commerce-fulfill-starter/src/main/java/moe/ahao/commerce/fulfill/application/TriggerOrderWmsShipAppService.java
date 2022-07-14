package moe.ahao.commerce.fulfill.application;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;
import moe.ahao.commerce.fulfill.api.event.TriggerOrderWmsShipEvent;
import moe.ahao.commerce.fulfill.application.processor.OrderDeliveredWmsEventProcessor;
import moe.ahao.commerce.fulfill.application.processor.OrderOutStockWmsEventProcessor;
import moe.ahao.commerce.fulfill.application.processor.OrderSignedWmsEventProcessor;
import moe.ahao.commerce.fulfill.application.processor.OrderWmsShipEventProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TriggerOrderWmsShipAppService implements ApplicationContextAware {
    @Setter
    private ApplicationContext applicationContext;

    public boolean trigger(TriggerOrderWmsShipEvent event) {
        // 1. 获取处理器
        OrderStatusChangeEnum orderStatusChange = event.getOrderStatusChange();
        OrderWmsShipEventProcessor processor = this.getWmsShipEventProcessor(orderStatusChange);

        // 2. 执行
        if (processor != null) {
            processor.execute(event);
        }

        return true;
    }

    /**
     * 订单物流配送结果处理器
     */
    private OrderWmsShipEventProcessor getWmsShipEventProcessor(OrderStatusChangeEnum orderStatusChange) {
        if (OrderStatusChangeEnum.ORDER_OUT_STOCKED.equals(orderStatusChange)) {
            // 订单已出库事件
            return applicationContext.getBean(OrderOutStockWmsEventProcessor.class);
        } else if (OrderStatusChangeEnum.ORDER_DELIVERED.equals(orderStatusChange)) {
            // 订单已配送事件
            return applicationContext.getBean(OrderDeliveredWmsEventProcessor.class);
        } else if (OrderStatusChangeEnum.ORDER_SIGNED.equals(orderStatusChange)) {
            // 订单已签收事件
            return applicationContext.getBean(OrderSignedWmsEventProcessor.class);
        }
        return null;
    }
}
