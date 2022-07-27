package moe.ahao.commerce.order.infrastructure.gateway.feign;

import moe.ahao.commerce.market.api.MarketFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-market", contextId = "market", path = MarketFeignApi.PATH)
public interface MarketFeignClient extends MarketFeignApi {
}
