package moe.ahao.commerce.pay.api.command;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付服务的退款入参
 */
@Data
public class RefundOrderCommand {
    /**
     * 订单id
     */
    public String orderId;
    /**
     * 实际退款金额
     */
    public BigDecimal refundAmount;
    /**
     * 售后单id
     */
    private String afterSaleId;
    /**
     * 交易流水号
     */
    private String outTradeNo;
}
