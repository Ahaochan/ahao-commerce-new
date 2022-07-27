package moe.ahao.commerce.product.api.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetProductSkuQuery {
    /**
     * 卖家ID
     */
    private String sellerId;
    /**
     * 商品skuCode
     */
    private String skuCode;
}
