package moe.ahao.commerce.customer.infrastructure.gateway;

import moe.ahao.commerce.customer.api.command.CustomerReceiveAfterSaleCommand;
import moe.ahao.commerce.customer.api.command.CustomerReviewReturnGoodsCommand;
import moe.ahao.commerce.customer.infrastructure.exception.CustomerExceptionEnum;
import moe.ahao.commerce.customer.infrastructure.gateway.feign.AfterSaleFeignClient;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单售后远程接口
 */
@Component
public class AfterSaleGateway {
    @Autowired
    private AfterSaleFeignClient afterSaleFeignClient;

    /**
     * 接收客服的审核结果
     */
    public Boolean receiveCustomerAuditResult(CustomerReviewReturnGoodsCommand command) {
        Result<Boolean> result = afterSaleFeignClient.receiveCustomerAuditResult(command);
        if (result.getCode() != Result.SUCCESS) {
            throw CustomerExceptionEnum.PROCESS_RECEIVE_AFTER_SALE.msg();
        }
        return result.getObj();
    }

    /**
     * 客服系统查询售后支付单信息
     */
    public String customerFindAfterSaleRefundInfo(CustomerReceiveAfterSaleCommand command) {
        Result<String> result = afterSaleFeignClient.customerFindAfterSaleRefundInfo(command);
        if (result.getCode() != Result.SUCCESS) {
            throw CustomerExceptionEnum.AFTER_SALE_REFUND_ID_IS_NULL.msg();
        }
        return result.getObj();
    }
}
