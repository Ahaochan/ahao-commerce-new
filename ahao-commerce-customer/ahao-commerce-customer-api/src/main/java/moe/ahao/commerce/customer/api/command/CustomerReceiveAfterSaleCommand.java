package moe.ahao.commerce.customer.api.command;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 客服接收订单系统的售后申请入参
 */
@Data
public class CustomerReceiveAfterSaleCommand {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 售后id
     */
    private String afterSaleId;
    /**
     * 售后类型 1 退款  2 退货
     */
    private Integer afterSaleType;
    /**
     * 实际退款金额
     */
    private BigDecimal returnGoodAmount;
    /**
     * 申请退款金额
     */
    private BigDecimal applyRefundAmount;
}
