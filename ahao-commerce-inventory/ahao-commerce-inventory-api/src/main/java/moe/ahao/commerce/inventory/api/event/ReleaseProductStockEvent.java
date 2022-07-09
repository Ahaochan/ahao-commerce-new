package moe.ahao.commerce.inventory.api.event;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 释放商品库存入参
 */
@Data
public class ReleaseProductStockEvent {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单条目
     */
    private List<OrderItem> orderItems;

    @Data
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
