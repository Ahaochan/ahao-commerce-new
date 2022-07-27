package moe.ahao.commerce.aftersale.api.command;

import lombok.Data;

/**
 * 取消订单入参
 */
@Data
public class CancelOrderCommand {
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
    /**
     * 原订单状态
     */
    private Integer oldOrderStatus;
}
