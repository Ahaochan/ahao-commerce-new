package moe.ahao.commerce.product.api.query;

import lombok.Data;

import java.util.List;

@Data
public class ListProductSkuQuery {
    /**
     * 卖家ID
     */
    private String sellerId;
    /**
     * 商品skuCode集合
     */
    private List<String> skuCodeList;
}
