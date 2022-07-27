package moe.ahao.commerce.order.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("order")
public class OrderProperties {
    public static final Integer ORDER_EXPIRE_TIME = 30 * 60 * 1000;
    /**
     * 订单超时支付时间限制, 单位毫秒, 默认30分钟
     */
    private Integer expireTime = ORDER_EXPIRE_TIME;
}
