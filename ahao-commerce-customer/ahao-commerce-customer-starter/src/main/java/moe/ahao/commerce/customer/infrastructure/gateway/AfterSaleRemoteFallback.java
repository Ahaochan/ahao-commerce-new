package moe.ahao.commerce.customer.infrastructure.gateway;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.customer.api.command.CustomerReviewReturnGoodsCommand;
import moe.ahao.commerce.customer.api.dto.CheckReceiveCustomerAuditResultDTO;
import org.springframework.stereotype.Component;

/**
 * 订单售后远程服务降级处理组件
 */
@Slf4j
@Component
public class AfterSaleRemoteFallback {
    /**
     * 接收客服的审核结果降级处理
     */
    public CheckReceiveCustomerAuditResultDTO receiveCustomerAuditResultFallback(CustomerReviewReturnGoodsCommand customerReviewReturnGoodsRequest, Throwable e) {
        log.error("接收客服审核结果触发降级了", e);
        CheckReceiveCustomerAuditResultDTO checkReceiveCustomerAuditResultDTO = new CheckReceiveCustomerAuditResultDTO();
        checkReceiveCustomerAuditResultDTO.setResult(true);
        return checkReceiveCustomerAuditResultDTO;
    }
}
