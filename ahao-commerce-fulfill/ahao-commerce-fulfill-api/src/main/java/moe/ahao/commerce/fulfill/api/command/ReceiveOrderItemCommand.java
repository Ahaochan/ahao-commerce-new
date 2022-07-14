package moe.ahao.commerce.fulfill.api.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 履约订单商品明细请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveOrderItemCommand {
    /**
     * 商品sku
     */
    private String skuCode;
    /**
     * 商品名称
     */
    private String productName;
    /**
     * 销售单价
     */
    private BigDecimal salePrice;
    /**
     * 销售数量
     */
    private BigDecimal saleQuantity;
    /**
     * 商品单位
     */
    private String productUnit;
    /**
     * 付款金额
     */
    private BigDecimal payAmount;
    /**
     * 当前商品支付原总价
     */
    private BigDecimal originAmount;
}
