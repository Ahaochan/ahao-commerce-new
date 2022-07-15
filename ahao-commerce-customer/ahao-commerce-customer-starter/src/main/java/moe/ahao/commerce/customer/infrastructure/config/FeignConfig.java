package moe.ahao.commerce.customer.infrastructure.config;

import moe.ahao.commerce.customer.infrastructure.gateway.feign.AfterSaleFeignClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = {AfterSaleFeignClient.class})
public class FeignConfig {
}
