package moe.ahao.commerce.order.infrastructure.gateway.feign;

import moe.ahao.commerce.fulfill.api.FulfillFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-fulfill", path = FulfillFeignApi.PATH)
public interface FulfillFeignClient extends FulfillFeignApi {
}
