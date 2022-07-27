package moe.ahao.commerce.order.infrastructure.gateway;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;
import moe.ahao.commerce.order.infrastructure.gateway.feign.ProductFeignClient;
import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.GetProductSkuQuery;
import moe.ahao.commerce.product.api.query.ListProductSkuQuery;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品服务远程接口
 */
@Component
public class ProductGateway {
    /**
     * 商品服务
     */
    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 查询商品信息
     */
    @SentinelResource(value = "ProductGateway:getBySkuCode")
    public ProductSkuDTO getBySkuCode(GetProductSkuQuery query) {
        Result<ProductSkuDTO> result = productFeignClient.getBySkuCode(query);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderException(result.getCode(), result.getMsg());
        }
        ProductSkuDTO obj = result.getObj();
        return obj;
    }

    /**
     * 批量查询商品信息
     */
    @SentinelResource(value = "ProductGateway:listBySkuCodes")
    public List<ProductSkuDTO> listBySkuCodes(ListProductSkuQuery query) {
        Result<List<ProductSkuDTO>> result = productFeignClient.listBySkuCodes(query);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderException(result.getCode(), result.getMsg());
        }
        List<ProductSkuDTO> obj = result.getObj();
        return obj;
    }
}
