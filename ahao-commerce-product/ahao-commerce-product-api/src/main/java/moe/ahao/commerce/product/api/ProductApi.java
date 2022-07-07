package moe.ahao.commerce.product.api;

import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.GetProductSkuQuery;
import moe.ahao.commerce.product.api.query.ListProductSkuQuery;
import moe.ahao.domain.entity.Result;

import java.util.List;

/* package */ interface ProductApi {
    /**
     * 查询商品SKU详情
     */
    Result<ProductSkuDTO> getBySkuCode(GetProductSkuQuery query);

    /**
     * 批量查询商品SKU详情
     */
    Result<List<ProductSkuDTO>> listBySkuCodes(ListProductSkuQuery query);
}
