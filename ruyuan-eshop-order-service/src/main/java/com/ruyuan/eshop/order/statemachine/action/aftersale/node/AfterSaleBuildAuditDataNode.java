package com.ruyuan.eshop.order.statemachine.action.aftersale.node;

import com.ruyuan.eshop.common.enums.CustomerAuditSourceEnum;
import com.ruyuan.eshop.customer.domain.request.CustomerReviewReturnGoodsRequest;
import com.ruyuan.eshop.order.converter.AfterSaleConverter;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.domain.request.CustomerAuditAssembleRequest;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 售后审核 组装审核数据 节点
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class AfterSaleBuildAuditDataNode extends StandardProcessor {

    @Autowired
    private AfterSaleConverter afterSaleConverter;

    @Override
    protected void processInternal(ProcessContext processContext) {
        AfterSaleStateMachineDTO afterSaleStateMachineDTO = processContext.get("afterSaleStateMachineDTO");
        AfterSaleInfoDO afterSaleInfoDO = processContext.get("afterSaleInfoDO");

        CustomerReviewReturnGoodsRequest customerReviewReturnGoodsRequest = afterSaleStateMachineDTO.getCustomerReviewReturnGoodsRequest();
        CustomerAuditAssembleRequest customerAuditAssembleRequest = afterSaleConverter.review2AuditPass(customerReviewReturnGoodsRequest);
        customerAuditAssembleRequest.setReviewTime(new Date());
        customerAuditAssembleRequest.setReviewSource(CustomerAuditSourceEnum.SELF_MALL.getCode());
        customerAuditAssembleRequest.setAfterSaleInfoDTO(afterSaleConverter.afterSaleInfoDO2DTO(afterSaleInfoDO));

        processContext.set("customerAuditAssembleRequest", customerAuditAssembleRequest);
    }
}
