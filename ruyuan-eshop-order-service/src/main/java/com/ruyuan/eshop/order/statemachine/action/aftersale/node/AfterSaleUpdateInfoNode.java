package com.ruyuan.eshop.order.statemachine.action.aftersale.node;

import com.ruyuan.eshop.common.enums.AfterSaleStateMachineChangeEnum;
import com.ruyuan.eshop.common.enums.AfterSaleStatusChangeEnum;
import com.ruyuan.eshop.common.enums.CustomerAuditResult;
import com.ruyuan.eshop.customer.domain.request.CustomerReviewReturnGoodsRequest;
import com.ruyuan.eshop.order.dao.AfterSaleInfoDAO;
import com.ruyuan.eshop.order.dao.AfterSaleLogDAO;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleLogDO;
import com.ruyuan.eshop.order.domain.request.CustomerAuditAssembleRequest;
import com.ruyuan.eshop.order.service.impl.AfterSaleOperateLogFactory;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
 * 售后审核 更新售后信息节点 节点
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class AfterSaleUpdateInfoNode extends StandardProcessor {

    @Autowired
    private AfterSaleOperateLogFactory afterSaleOperateLogFactory;

    @Autowired
    private AfterSaleLogDAO afterSaleLogDAO;

    @Autowired
    private AfterSaleInfoDAO afterSaleInfoDAO;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    protected void processInternal(ProcessContext processContext) {
        //  @Transactional无法生效，需要用编程式事务
        transactionTemplate.execute(transactionStatus -> {
            CustomerAuditAssembleRequest customerAuditAssembleRequest = processContext.get("customerAuditAssembleRequest");
            AfterSaleInfoDO afterSaleInfoDO = processContext.get("afterSaleInfoDO");
            AfterSaleStateMachineChangeEnum event = processContext.get("event");

            //  前台传入的审核结果 成功 or 失败
            AfterSaleStateMachineDTO afterSaleStateMachineDTO = processContext.get("afterSaleStateMachineDTO");
            CustomerReviewReturnGoodsRequest customerReviewReturnGoodsRequest = afterSaleStateMachineDTO.getCustomerReviewReturnGoodsRequest();
            Integer auditResult = customerReviewReturnGoodsRequest.getAuditResult();

            AfterSaleLogDO afterSaleLogDO = new AfterSaleLogDO();
            //  审核拒绝
            if (CustomerAuditResult.REJECT.getCode().equals(auditResult)) {
                customerAuditAssembleRequest.setReviewReason(CustomerAuditResult.REJECT.getMsg());
                afterSaleLogDO = afterSaleOperateLogFactory.get(afterSaleInfoDO, AfterSaleStatusChangeEnum.AFTER_SALE_REVIEWED_REJECTION);
            }
            //  审核通过
            if (CustomerAuditResult.ACCEPT.getCode().equals(auditResult)) {
                customerAuditAssembleRequest.setReviewReason(CustomerAuditResult.ACCEPT.getMsg());
                afterSaleLogDO = afterSaleOperateLogFactory.get(afterSaleInfoDO, AfterSaleStatusChangeEnum.AFTER_SALE_REVIEWED_PASS);
            }

            //  更新售后信息
            afterSaleInfoDAO.updateCustomerAuditAfterSaleResult(event.getToStatus().getCode(), customerAuditAssembleRequest);

            //  记录售后日志
            afterSaleLogDO.setOrderId(afterSaleInfoDO.getOrderId());
            afterSaleLogDAO.save(afterSaleLogDO);
            return true;
        });
    }
}
