package com.ruyuan.eshop.order.statemachine.action.aftersale.node;

import com.ruyuan.consistency.annotation.ConsistencyTask;
import com.ruyuan.eshop.order.domain.request.AuditPassReleaseAssetsRequest;
import com.ruyuan.eshop.order.service.RocketMqService;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 售后审核 客服审核通过后释放权益资产消息(释放库存和实际退款) 节点
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class AfterSaleAuditPassSendMessageNode extends StandardProcessor {

    @Autowired
    private RocketMqService rocketMqService;

    @Override
    protected void processInternal(ProcessContext processContext) {
        AuditPassReleaseAssetsRequest auditPassReleaseAssetsRequest = processContext.get("auditPassReleaseAssetsRequest");

        rocketMqService.sendAuditPassMessage(auditPassReleaseAssetsRequest);
    }
}
