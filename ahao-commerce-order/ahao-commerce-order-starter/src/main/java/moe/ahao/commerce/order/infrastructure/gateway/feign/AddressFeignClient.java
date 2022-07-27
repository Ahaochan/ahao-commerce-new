package moe.ahao.commerce.order.infrastructure.gateway.feign;

import moe.ahao.commerce.address.api.AddressFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-address", path = AddressFeignApi.PATH)
public interface AddressFeignClient extends AddressFeignApi {
}
