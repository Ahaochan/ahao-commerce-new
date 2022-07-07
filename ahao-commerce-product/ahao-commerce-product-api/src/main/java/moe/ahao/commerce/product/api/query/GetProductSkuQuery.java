package moe.ahao.commerce.product.api.query;

import lombok.Data;

@Data
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
