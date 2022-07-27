package moe.ahao.commerce.order.infrastructure.gateway.feign;

import moe.ahao.commerce.product.api.ProductFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-product", path = ProductFeignApi.PATH)
public interface ProductFeignClient extends ProductFeignApi {
}
