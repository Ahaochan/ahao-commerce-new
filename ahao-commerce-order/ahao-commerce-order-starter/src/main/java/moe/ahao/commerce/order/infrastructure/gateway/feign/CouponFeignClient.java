package moe.ahao.commerce.order.infrastructure.gateway.feign;

import moe.ahao.commerce.market.api.CouponFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-market", contextId = "coupon", path = CouponFeignApi.PATH)
public interface CouponFeignClient extends CouponFeignApi {
}
