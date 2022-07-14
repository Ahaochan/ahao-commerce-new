package moe.ahao.commerce.fulfill.application.processor;

import moe.ahao.commerce.fulfill.api.event.TriggerOrderWmsShipEvent;

/**
 * 订单物流配送结果处理器
 */
public interface OrderWmsShipEventProcessor {
    /**
     * 执行
     */
    void execute(TriggerOrderWmsShipEvent event);
}
