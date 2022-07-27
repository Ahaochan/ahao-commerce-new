package moe.ahao.commerce.order.infrastructure.gateway;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;
import moe.ahao.commerce.order.infrastructure.gateway.fallback.RiskGatewayFallback;
import moe.ahao.commerce.order.infrastructure.gateway.feign.RiskFeignClient;
import moe.ahao.commerce.risk.api.command.CheckOrderRiskCommand;
import moe.ahao.commerce.risk.api.dto.CheckOrderRiskDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 营销服务远程接口
 */
@Component
public class RiskGateway {
    /**
     * 风控服务
     */
    @Autowired
    private RiskFeignClient riskFeignClient;

    /**
     * 订单风控检查
     */
    @SentinelResource(value = "RiskGateway:checkOrderRisk", fallbackClass = RiskGatewayFallback.class, fallback = "checkOrderRiskFallback")
    public CheckOrderRiskDTO checkOrderRisk(CheckOrderRiskCommand command) {
        Result<CheckOrderRiskDTO> result = riskFeignClient.checkOrderRisk(command);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderException(result.getCode(), result.getMsg());
        }
        return result.getObj();
    }
}
