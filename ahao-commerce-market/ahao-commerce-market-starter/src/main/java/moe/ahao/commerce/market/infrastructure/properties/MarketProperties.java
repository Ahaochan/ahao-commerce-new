package moe.ahao.commerce.market.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Data
@Component
@ConfigurationProperties("market")
public class MarketProperties {
    // 默认的标准运费 5元
    private BigDecimal defaultShippingAmount = new BigDecimal("5");
    // 默认情况下满 49 元免运费
    private BigDecimal defaultConditionAmount = new BigDecimal("49");
}
