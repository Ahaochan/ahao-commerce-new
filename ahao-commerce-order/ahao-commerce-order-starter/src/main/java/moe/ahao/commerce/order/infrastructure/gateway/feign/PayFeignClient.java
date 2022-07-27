package moe.ahao.commerce.order.infrastructure.gateway.feign;

import moe.ahao.commerce.pay.api.PayFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-pay", path = PayFeignApi.PATH)
public interface PayFeignClient extends PayFeignApi {
}
