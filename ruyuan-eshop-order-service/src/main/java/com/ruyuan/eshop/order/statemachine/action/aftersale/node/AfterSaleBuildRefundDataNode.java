package com.ruyuan.eshop.order.statemachine.action.aftersale.node;

import com.ruyuan.eshop.common.enums.AfterSaleTypeEnum;
import com.ruyuan.eshop.common.message.RefundMessage;
import com.ruyuan.eshop.order.domain.request.AuditPassReleaseAssetsRequest;
import com.ruyuan.eshop.order.domain.request.CustomerAuditAssembleRequest;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import org.springframework.stereotype.Component;

/**
 * 售后审核 组装实际退款参数 节点
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class AfterSaleBuildRefundDataNode extends StandardProcessor {

    @Override
    protected void processInternal(ProcessContext processContext) {
        CustomerAuditAssembleRequest customerAuditAssembleRequest = processContext.get("customerAuditAssembleRequest");
        AuditPassReleaseAssetsRequest auditPassReleaseAssetsRequest = processContext.get("auditPassReleaseAssetsRequest");

        //  实际退款数据
        RefundMessage refundMessage = new RefundMessage();
        refundMessage.setOrderId(customerAuditAssembleRequest.getOrderId());
        refundMessage.setAfterSaleId(customerAuditAssembleRequest.getAfterSaleId());
        refundMessage.setSkuCode(customerAuditAssembleRequest.getSkuCode());
        refundMessage.setAfterSaleType(AfterSaleTypeEnum.RETURN_GOODS.getCode());

        auditPassReleaseAssetsRequest.setRefundMessage(refundMessage);
        processContext.set("auditPassReleaseAssetsRequest", auditPassReleaseAssetsRequest);
    }
}
