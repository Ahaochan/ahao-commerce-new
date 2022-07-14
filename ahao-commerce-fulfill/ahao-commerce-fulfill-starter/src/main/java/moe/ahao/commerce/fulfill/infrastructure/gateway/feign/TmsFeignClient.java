package moe.ahao.commerce.fulfill.infrastructure.gateway.feign;

import moe.ahao.commerce.tms.api.TmsFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-tms", path = TmsFeignApi.PATH)
public interface TmsFeignClient extends TmsFeignApi {
}
