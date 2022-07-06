package moe.ahao.commerce.market.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculateOrderAmountDTO {
    /**
     * 优惠券
     */
    private UserCouponDTO userCoupon;
    /**
     * 订单费用信息
     */
    private List<OrderAmountDTO> orderAmountList;
    /**
     * 订单条目费用信息
     */
    private List<OrderItemAmountDTO> orderItemAmountList;

    /**
     * 营销计算出来的费用信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderAmountDTO {
        /**
         * 订单Id
         */
        private String orderId;
        /**
         * 收费类型
         */
        private Integer amountType;
        /**
         * 收费金额
         */
        private BigDecimal amount;
    }

    /**
     * 营销计算出来的订单条目费用信息
     */
    @Data
    public static class OrderItemAmountDTO {
        /**
         * 订单Id
         */
        private String orderId;
        /**
         * 产品类型
         */
        private Integer productType;
        /**
         * sku编码
         */
        private String skuCode;
        /**
         * 销售数量
         */
        private BigDecimal saleQuantity;
        /**
         * 销售单价
         */
        private BigDecimal salePrice;
        /**
         * 收费类型
         */
        private Integer amountType;
        /**
         * 收费金额
         */
        private BigDecimal amount;
    }
}
