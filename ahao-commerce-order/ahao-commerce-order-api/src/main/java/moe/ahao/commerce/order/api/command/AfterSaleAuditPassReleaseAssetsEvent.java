package moe.ahao.commerce.order.api.command;

import lombok.Data;

/**
 * 客服审核通过后发送释放资产入参
 */
@Data
public class AfterSaleAuditPassReleaseAssetsEvent {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 售后单id
     */
    private String afterSaleId;
}
