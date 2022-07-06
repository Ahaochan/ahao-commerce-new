package moe.ahao.commerce.risk.api;

import moe.ahao.commerce.risk.api.command.CheckOrderRiskCommand;
import moe.ahao.commerce.risk.api.dto.CheckOrderRiskDTO;
import moe.ahao.domain.entity.Result;

/**
 * 风控服务API
 */
public interface RiskApi {
    /**
     * 订单风控检查
     */
    Result<CheckOrderRiskDTO> checkOrderRisk(CheckOrderRiskCommand command);
}
