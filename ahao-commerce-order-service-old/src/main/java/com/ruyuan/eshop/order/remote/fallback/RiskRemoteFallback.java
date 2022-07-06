package com.ruyuan.eshop.order.remote.fallback;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.risk.api.command.CheckOrderRiskCommand;
import moe.ahao.commerce.risk.api.dto.CheckOrderRiskDTO;
import org.springframework.stereotype.Component;

/**
 * 风控远程服务降级处理组件
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class RiskRemoteFallback {

    /**
     * 订单风控检查降级处理
     * @param checkOrderRiskRequest
     * @param e
     * @return
     */
    public CheckOrderRiskDTO checkOrderRiskFallback(CheckOrderRiskCommand checkOrderRiskRequest, Throwable e) {
        log.error("订单风控检查触发降级了", e);
        CheckOrderRiskDTO checkOrderRiskDTO = new CheckOrderRiskDTO();
        checkOrderRiskDTO.setResult(true);
        return checkOrderRiskDTO;
    }
}
