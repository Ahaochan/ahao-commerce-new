package moe.ahao.commerce.fulfill.infrastructure.gateway.feign;

import moe.ahao.commerce.wms.api.WmsFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-wms", path = WmsFeignApi.PATH)
public interface WmsFeignClient extends WmsFeignApi {
}
