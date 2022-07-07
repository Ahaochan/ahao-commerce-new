package moe.ahao.commerce.product.api;

import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.GetProductSkuQuery;
import moe.ahao.commerce.product.api.query.ListProductSkuQuery;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ProductFeignApi extends ProductApi {
    String CONTEXT = "/api/product";

    @PostMapping("/getBySkuCode")
    Result<ProductSkuDTO> getBySkuCode(@RequestBody GetProductSkuQuery query);
    @PostMapping("/listBySkuCodes")
    Result<List<ProductSkuDTO>> listBySkuCodes(@RequestBody ListProductSkuQuery query);
}
