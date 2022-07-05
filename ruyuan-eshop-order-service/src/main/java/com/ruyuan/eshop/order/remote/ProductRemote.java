package com.ruyuan.eshop.order.remote;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.product.api.ProductApi;
import com.ruyuan.eshop.product.domain.dto.ProductSkuDTO;
import com.ruyuan.eshop.product.domain.query.GetProductSkuQuery;
import com.ruyuan.eshop.product.domain.query.ListProductSkuQuery;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品服务远程接口
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class ProductRemote {

    /**
     * 商品服务
     */
    @DubboReference(version = "1.0.0")
    private ProductApi productApi;

    /**
     * 查询商品信息
     * @param skuCode
     * @param sellerId
     * @return
     */
    @SentinelResource(value = "ProductRemote:getProductSku")
    public ProductSkuDTO getProductSku(String skuCode, String sellerId) {
        GetProductSkuQuery productSkuQuery = new GetProductSkuQuery();
        productSkuQuery.setSkuCode(skuCode);
        productSkuQuery.setSellerId(sellerId);
        JsonResult<ProductSkuDTO> jsonResult = productApi.getProductSku(productSkuQuery);
        if (!jsonResult.getSuccess()) {
            throw new OrderBizException(jsonResult.getErrorCode(), jsonResult.getErrorMessage());
        }
        return jsonResult.getData();
    }

    /**
     * 批量查询商品信息
     * @param skuCodeList
     * @param sellerId
     * @return
     */
    @SentinelResource(value = "ProductRemote:listProductSku")
    public List<ProductSkuDTO> listProductSku(List<String> skuCodeList, String sellerId) {
        ListProductSkuQuery productSkuQuery = new ListProductSkuQuery();
        productSkuQuery.setSkuCodeList(skuCodeList);
        productSkuQuery.setSellerId(sellerId);
        JsonResult<List<ProductSkuDTO>> jsonResult = productApi.listProductSku(productSkuQuery);
        if (!jsonResult.getSuccess()) {
            throw new OrderBizException(jsonResult.getErrorCode(), jsonResult.getErrorMessage());
        }
        return jsonResult.getData();
    }

}