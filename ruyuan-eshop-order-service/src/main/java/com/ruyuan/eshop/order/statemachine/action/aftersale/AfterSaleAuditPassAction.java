package com.ruyuan.eshop.order.statemachine.action.aftersale;

import com.ruyuan.eshop.common.enums.AfterSaleStateMachineChangeEnum;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.statemachine.action.AfterSaleStateAction;
import com.ruyuan.process.engine.process.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 售后客服审核Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class AfterSaleAuditPassAction extends AfterSaleStateAction<AfterSaleStateMachineDTO> {

    @Override
    public AfterSaleStateMachineChangeEnum event() {
        return AfterSaleStateMachineChangeEnum.AUDIT_PASS;
    }

    @Override
    protected AfterSaleStateMachineDTO onStateChangeInternal(AfterSaleStateMachineChangeEnum event,
                                                             AfterSaleStateMachineDTO afterSaleStateMachineDTO) {
        //  客服审核通过
        ProcessContext afterSaleProcess = processContextFactory.getContext("afterSaleAuditProcess");
        afterSaleProcess.set("afterSaleStateMachineDTO", afterSaleStateMachineDTO);
        afterSaleProcess.set("event", event);
        afterSaleProcess.start();
        return afterSaleStateMachineDTO;
    }
}
