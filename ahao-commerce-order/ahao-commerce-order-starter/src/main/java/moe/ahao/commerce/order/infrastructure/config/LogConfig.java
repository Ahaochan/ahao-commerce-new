package moe.ahao.commerce.order.infrastructure.config;

import moe.ahao.aop.RequestMappingLogAOP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class LogConfig {
    @Bean
    public RequestMappingLogAOP requestMappingLogAOP() {
        return new RequestMappingLogAOP();
    }
}
