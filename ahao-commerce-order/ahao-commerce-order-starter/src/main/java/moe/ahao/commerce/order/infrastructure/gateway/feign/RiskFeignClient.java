package moe.ahao.commerce.order.infrastructure.gateway.feign;

import moe.ahao.commerce.risk.api.RiskFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-risk", path = RiskFeignApi.PATH)
public interface RiskFeignClient extends RiskFeignApi {
}
