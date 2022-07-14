package moe.ahao.commerce.product.api;

import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.GetProductSkuQuery;
import moe.ahao.commerce.product.api.query.ListProductSkuQuery;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ProductFeignApi {
    String PATH = "/api/product";

    /**
     * 查询商品SKU详情
     */
    @PostMapping("/getBySkuCode")
    Result<ProductSkuDTO> getBySkuCode(@RequestBody GetProductSkuQuery query);

    /**
     * 批量查询商品SKU详情
     */
    @PostMapping("/listBySkuCodes")
    Result<List<ProductSkuDTO>> listBySkuCodes(@RequestBody ListProductSkuQuery query);
}
