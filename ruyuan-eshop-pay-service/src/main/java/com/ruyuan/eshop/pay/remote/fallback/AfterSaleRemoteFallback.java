package com.ruyuan.eshop.pay.remote.fallback;

import com.ruyuan.eshop.customer.domain.request.CustomerReviewReturnGoodsRequest;
import com.ruyuan.eshop.pay.domain.dto.CheckCustomerReviewReturnGoodsRequestDTO;
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
     * 取消订单支付退款回调降级处理
     */
    public CheckCustomerReviewReturnGoodsRequestDTO refundCallbackFallback(CustomerReviewReturnGoodsRequest customerReviewReturnGoodsRequest, Throwable e) {
        log.error("取消订单支付退款回调触发降级了", e);
        CheckCustomerReviewReturnGoodsRequestDTO checkCustomerReviewReturnGoodsRequestDTO = new CheckCustomerReviewReturnGoodsRequestDTO();
        checkCustomerReviewReturnGoodsRequestDTO.setResult(true);
        return checkCustomerReviewReturnGoodsRequestDTO;
    }
}
