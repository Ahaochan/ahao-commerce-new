package moe.ahao.commerce.pay.infrastructure.gateway.impl;

import moe.ahao.commerce.pay.api.command.RefundOrderCallbackCommand;
import org.springframework.stereotype.Component;


/**
 * 订单售后远程接口
 * TODO 改造Feign调用
 */
@Component
public class AfterSaleRemote {

    // @DubboReference(version = "1.0.0")
    // private AfterSaleApi afterSaleApi;

    /**
     * 取消订单支付退款回调
     */
    // @SentinelResource(value = "AfterSaleRemote:refundCallback", fallbackClass = AfterSaleRemoteFallback.class, fallback = "refundCallbackFallback")
    public Boolean refundOrderCallback(RefundOrderCallbackCommand payRefundCallbackRequest) {
        // Result<Boolean> jsonResult = afterSaleApi.refundCallback(payRefundCallbackRequest);
        // if (!jsonResult.getSuccess()) {
        //     throw new PayException(jsonResult.getErrorCode(), jsonResult.getErrorMessage());
        // }
        return true;
    }

}
