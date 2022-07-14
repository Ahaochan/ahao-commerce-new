package moe.ahao.commerce.fulfill.api.event;

import lombok.Data;

/**
 * 物流配送结果事件基类
 */
@Data
class BaseWmsShipEvent {
    /**
     * 订单id
     */
    private String orderId;
}
