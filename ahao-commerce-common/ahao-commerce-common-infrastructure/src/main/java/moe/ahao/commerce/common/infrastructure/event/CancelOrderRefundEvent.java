package moe.ahao.commerce.common.infrastructure.event;

import lombok.Data;

/**
 * 取消订单退款
 */
@Data
public class CancelOrderRefundEvent {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单取消类型 0-手动取消 1-超时未支付
     */
    private Integer cancelType;
}
