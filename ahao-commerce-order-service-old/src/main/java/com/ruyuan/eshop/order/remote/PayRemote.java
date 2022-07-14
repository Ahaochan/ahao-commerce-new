package com.ruyuan.eshop.order.remote;

import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import moe.ahao.commerce.pay.api.PayFeignApi;
import moe.ahao.commerce.pay.api.command.PayOrderCommand;
import moe.ahao.commerce.pay.api.command.RefundOrderCommand;
import moe.ahao.commerce.pay.api.dto.PayOrderDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 支付服务远程接口
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class PayRemote {

    /**
     * 支付服务
     */
    @Autowired
    private PayFeignApi payApi;

    /**
     * 调用支付系统进行预支付下单
     * @param payOrderRequest
     */
    public PayOrderDTO payOrder(PayOrderCommand payOrderRequest) {
        Result<PayOrderDTO> result = payApi.payOrder(payOrderRequest);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_PRE_PAY_ERROR);
        }
        return result.getObj();
    }

    /**
     * 调用支付系统执行退款
     * @param payRefundRequest
     */
    public void executeRefund(RefundOrderCommand payRefundRequest) {
        Result<Boolean> result = payApi.refundOrder(payRefundRequest);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_REFUND_AMOUNT_FAILED);
        }
    }

}
