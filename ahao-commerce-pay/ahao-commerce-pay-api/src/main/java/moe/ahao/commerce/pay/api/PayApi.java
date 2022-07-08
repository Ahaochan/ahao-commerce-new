package moe.ahao.commerce.pay.api;

import moe.ahao.commerce.pay.api.command.PayOrderCommand;
import moe.ahao.commerce.pay.api.command.RefundOrderCommand;
import moe.ahao.commerce.pay.api.dto.PayOrderDTO;
import moe.ahao.domain.entity.Result;

public interface PayApi {
    /**
     * 支付订单
     */
    Result<PayOrderDTO> payOrder(PayOrderCommand command);

    /**
     * 调用支付接口执行退款
     */
    Result<Boolean> refundOrder(RefundOrderCommand command);
}
