package moe.ahao.commerce.common.infrastructure.event;

import lombok.Data;

/**
 * 订单支付超时自定取消订单延迟消息
 */
@Data
public class PayOrderTimeoutEvent {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 接入方业务线标识  1, "自营商城"
     */
    private Integer businessIdentifier;
    /**
     * 订单取消类型 0-手动取消 1-超时未支付
     */
    private Integer cancelType;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 订单类型
     */
    private Integer orderType;
    /**
     * 订单状态
     */
    private Integer orderStatus;
}
