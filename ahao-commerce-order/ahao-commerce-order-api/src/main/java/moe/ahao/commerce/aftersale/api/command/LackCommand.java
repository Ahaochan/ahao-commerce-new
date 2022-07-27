package moe.ahao.commerce.aftersale.api.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * 订单缺品请求
 */
@Data
public class LackCommand {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 具体的缺品项
     */
    private Set<LackItem> lackItems;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LackItem that = (LackItem) o;
            return Objects.equals(skuCode, that.skuCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(skuCode);
        }
    }
}
