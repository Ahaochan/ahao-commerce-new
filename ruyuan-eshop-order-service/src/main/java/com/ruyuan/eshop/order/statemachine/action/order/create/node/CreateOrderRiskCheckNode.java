package com.ruyuan.eshop.order.statemachine.action.order.create.node;

import com.ruyuan.eshop.order.converter.OrderConverter;
import com.ruyuan.eshop.order.domain.request.CreateOrderRequest;
import com.ruyuan.eshop.order.remote.RiskRemote;
import com.ruyuan.eshop.risk.domain.request.CheckOrderRiskRequest;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 创建订单风控检查
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class CreateOrderRiskCheckNode extends StandardProcessor {

    @Resource
    private RiskRemote riskRemote;

    @Resource
    private OrderConverter orderConverter;

    @Override
    protected void processInternal(ProcessContext processContext) {
        CreateOrderRequest createOrderRequest = processContext.get("createOrderRequest");
        // 调用风控服务进行风控检查
        CheckOrderRiskRequest checkOrderRiskRequest = orderConverter.convertRiskRequest(createOrderRequest);
        riskRemote.checkOrderRisk(checkOrderRiskRequest);
    }
}
