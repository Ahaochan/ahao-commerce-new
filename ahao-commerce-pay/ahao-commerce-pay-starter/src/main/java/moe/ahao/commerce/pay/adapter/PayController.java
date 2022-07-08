package moe.ahao.commerce.pay.adapter;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.pay.api.PayFeignApi;
import moe.ahao.commerce.pay.api.command.PayOrderCommand;
import moe.ahao.commerce.pay.api.command.RefundOrderCommand;
import moe.ahao.commerce.pay.api.dto.PayOrderDTO;
import moe.ahao.commerce.pay.application.PayOrderAppService;
import moe.ahao.commerce.pay.application.RefundOrderAppService;
import moe.ahao.commerce.pay.application.RefundOrderCallbackAppService;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(PayFeignApi.CONTEXT)
public class PayController implements PayFeignApi {
    @Autowired
    private PayOrderAppService payOrderAppService;
    @Autowired
    private RefundOrderAppService refundOrderAppService;
    @Autowired
    private RefundOrderCallbackAppService refundOrderCallbackAppService;

    @Override
    public Result<PayOrderDTO> payOrder(PayOrderCommand command) {
        PayOrderDTO payOrderDTO = payOrderAppService.pay(command);
        return Result.success(payOrderDTO);
    }

    @Override
    public Result<Boolean> refundOrder(RefundOrderCommand command) {
        boolean success = refundOrderAppService.refund(command);
        return Result.success(success);
    }

    /**
     * 取消订单支付退款回调
     */
    @PostMapping("/refundCallback")
    public Result<Boolean> refundCallback(HttpServletRequest request) {
        boolean success = refundOrderCallbackAppService.callback(request);
        return Result.success(success);
    }
}
