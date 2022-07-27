package moe.ahao.commerce.order.infrastructure.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单缺品信息
 */
@Data
public class OrderLackInfoDTO {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 具体的缺品项
     */
    private List<LackItem> lackItems;
    /**
     * 申请退款金额
     */
    private BigDecimal applyRefundAmount;
    /**
     * 实际退款金额
     */
    private BigDecimal realRefundAmount;

    /**
     * 具体的缺品项
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LackItem {
        /**
         * sku编码
         */
        private String skuCode;

        /**
         * 缺品数量
         */
        private BigDecimal lackNum;
    }
}
