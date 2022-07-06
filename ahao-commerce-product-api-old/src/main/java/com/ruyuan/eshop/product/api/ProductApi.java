package com.ruyuan.eshop.product.api;

import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.product.domain.dto.ProductSkuDTO;
import com.ruyuan.eshop.product.domain.query.GetProductSkuQuery;
import com.ruyuan.eshop.product.domain.query.ListProductSkuQuery;

import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
public interface ProductApi {

    /**
     * 查询商品SKU详情
     *
     * @param productSkuQuery
     * @return
     */
    JsonResult<ProductSkuDTO> getProductSku(GetProductSkuQuery productSkuQuery);

    /**
     * 批量查询商品SKU详情
     *
     * @param productSkuQuery
     * @return
     */
    JsonResult<List<ProductSkuDTO>> listProductSku(ListProductSkuQuery productSkuQuery);

}