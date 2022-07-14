package com.ruyuan.eshop.order.remote;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.remote.fallback.RiskRemoteFallback;
import moe.ahao.commerce.risk.api.RiskFeignApi;
import moe.ahao.commerce.risk.api.command.CheckOrderRiskCommand;
import moe.ahao.commerce.risk.api.dto.CheckOrderRiskDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 营销服务远程接口
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class RiskRemote {

    /**
     * 风控服务
     */
    @Autowired
    private RiskFeignApi riskApi;

    /**
     * 订单风控检查
     * @param checkOrderRiskRequest
     * @return
     */
    @SentinelResource(value = "RiskRemote:checkOrderRisk",
            fallbackClass = RiskRemoteFallback.class,
            fallback = "checkOrderRiskFallback")
    public CheckOrderRiskDTO checkOrderRisk(CheckOrderRiskCommand checkOrderRiskRequest) {
        Result<CheckOrderRiskDTO> result = riskApi.checkOrderRisk(checkOrderRiskRequest);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderBizException(String.valueOf(result.getCode()), result.getMsg());
        }
        return result.getObj();
    }

}
