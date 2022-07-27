package moe.ahao.commerce.order.infrastructure.config;

import moe.ahao.commerce.order.infrastructure.gateway.feign.*;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = {AddressFeignClient.class, CouponFeignClient.class, MarketFeignClient.class, ProductFeignClient.class, RiskFeignClient.class, InventoryFeignClient.class, PayFeignClient.class, FulfillFeignClient.class})
public class FeignConfig {
}
