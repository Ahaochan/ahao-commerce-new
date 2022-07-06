package com.ruyuan.eshop.customer.remote.fallback;

import com.ruyuan.eshop.customer.domain.dto.CheckReceiveCustomerAuditResultDTO;
import com.ruyuan.eshop.customer.domain.request.CustomerReviewReturnGoodsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单售后远程服务降级处理组件
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class AfterSaleRemoteFallback {
    /**
     * 接收客服的审核结果降级处理
     */
    public CheckReceiveCustomerAuditResultDTO receiveCustomerAuditResultFallback(CustomerReviewReturnGoodsRequest customerReviewReturnGoodsRequest, Throwable e) {
        log.error("接收客服审核结果触发降级了", e);
        CheckReceiveCustomerAuditResultDTO checkReceiveCustomerAuditResultDTO = new CheckReceiveCustomerAuditResultDTO();
        checkReceiveCustomerAuditResultDTO.setResult(true);
        return checkReceiveCustomerAuditResultDTO;
    }
}
