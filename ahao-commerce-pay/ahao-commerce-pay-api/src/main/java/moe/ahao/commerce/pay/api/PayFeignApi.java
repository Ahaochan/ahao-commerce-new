package moe.ahao.commerce.pay.api;

import moe.ahao.commerce.pay.api.command.PayOrderCommand;
import moe.ahao.commerce.pay.api.command.RefundOrderCommand;
import moe.ahao.commerce.pay.api.dto.PayOrderDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface PayFeignApi {
    String PATH = "/api/pay/";

    /**
     * 支付订单
     */
    @PostMapping("/payOrder")
    Result<PayOrderDTO> payOrder(@RequestBody PayOrderCommand command);

    /**
     * 调用支付接口执行退款
     */
    @PostMapping("/refundOrder")
    Result<Boolean> refundOrder(@RequestBody RefundOrderCommand command);
}
