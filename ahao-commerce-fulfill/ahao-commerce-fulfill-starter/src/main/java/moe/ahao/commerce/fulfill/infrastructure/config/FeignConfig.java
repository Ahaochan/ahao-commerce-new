package moe.ahao.commerce.fulfill.infrastructure.config;

import moe.ahao.commerce.fulfill.infrastructure.gateway.feign.TmsFeignClient;
import moe.ahao.commerce.fulfill.infrastructure.gateway.feign.WmsFeignClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = {TmsFeignClient.class, WmsFeignClient.class})
public class FeignConfig {
}
