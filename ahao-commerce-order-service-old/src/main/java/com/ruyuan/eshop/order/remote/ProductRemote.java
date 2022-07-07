package com.ruyuan.eshop.order.remote;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.ruyuan.eshop.order.exception.OrderBizException;
import moe.ahao.commerce.product.api.ProductDubboApi;
import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.GetProductSkuQuery;
import moe.ahao.commerce.product.api.query.ListProductSkuQuery;
import moe.ahao.domain.entity.Result;
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
    private ProductDubboApi productApi;

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
        Result<ProductSkuDTO> result = productApi.getBySkuCode(productSkuQuery);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderBizException(String.valueOf(result.getCode()), result.getMsg());
        }
        return result.getObj();
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
        Result<List<ProductSkuDTO>> result = productApi.listBySkuCodes(productSkuQuery);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderBizException(String.valueOf(result.getCode()), result.getMsg());
        }
        return result.getObj();
    }

}
