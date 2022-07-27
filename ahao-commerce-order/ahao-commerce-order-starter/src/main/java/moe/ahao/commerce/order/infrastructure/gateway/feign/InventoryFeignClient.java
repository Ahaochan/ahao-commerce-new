package moe.ahao.commerce.order.infrastructure.gateway.feign;

import moe.ahao.commerce.inventory.api.InventoryFeignApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ahao-commerce-inventory", path = InventoryFeignApi.PATH)
public interface InventoryFeignClient extends InventoryFeignApi {
}
