package moe.ahao.commerce.inventory.api.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 释放商品库存入参
 */
@Data
public class ReleaseProductStockCommand {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单条目
     */
    private List<OrderItem> orderItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        /**
         * 商品sku编号
         */
        private String skuCode;
        /**
         * 销售数量
         */
        private BigDecimal saleQuantity;
    }
}
