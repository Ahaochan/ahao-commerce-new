package moe.ahao.commerce.market.api.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculateOrderAmountQuery {
    /**
     * 订单Id
     */
    private String orderId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 卖家ID
     */
    private String sellerId;
    /**
     * 优惠券ID
     */
    private String couponId;
    /**
     * 区域ID
     */
    private String regionId;
    /**
     * 订单条目信息
     */
    private List<OrderItem> orderItemList;
    /**
     * 订单费用信息
     */
    private List<OrderAmount> orderAmountList;

    /**
     * 订单条目信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        /**
         * 商品Id
         */
        private String productId;
        /**
         * 商品类型 1:普通商品,2:预售商品
         */
        private Integer productType;
        /**
         * 商品sku编号
         */
        private String skuCode;
        /**
         * 商品销售价格
         */
        private BigDecimal salePrice;
        /**
         * 销售数量
         */
        private BigDecimal saleQuantity;
    }

    /**
     * 订单费用信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderAmount {
        /**
         * 费用类型
         */
        private Integer amountType;
        /**
         * 费用金额
         */
        private BigDecimal amount;
    }
}
