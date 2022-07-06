package moe.ahao.commerce.risk.api;

import moe.ahao.commerce.risk.api.command.CheckOrderRiskCommand;
import moe.ahao.commerce.risk.api.dto.CheckOrderRiskDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 风控服务API
 */
public interface RiskFeignApi extends RiskApi{
    String CONTEXT = "/api/risk/";

    /**
     * 订单风控检查
     */
    @PostMapping("/checkOrderRisk")
    Result<CheckOrderRiskDTO> checkOrderRisk(CheckOrderRiskCommand command);
}
