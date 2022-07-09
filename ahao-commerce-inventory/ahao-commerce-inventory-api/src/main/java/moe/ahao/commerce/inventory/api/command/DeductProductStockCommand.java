package moe.ahao.commerce.inventory.api.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 锁定商品库存入参
 */
@Data
public class DeductProductStockCommand {
    /**
     * 业务线标识
     */
    private Integer businessIdentifier;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 卖家id
     */
    private String sellerId;
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
