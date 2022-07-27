package moe.ahao.commerce.aftersale.api;

import moe.ahao.commerce.aftersale.api.command.*;
import moe.ahao.commerce.aftersale.api.dto.LackDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单中心-逆向售后业务接口
 */
public interface AfterSaleFeignApi {
    String PATH = "/api/aftersale";

    /**
     * 用户发起退货售后
     */
    @PostMapping("/applyAfterSale")
    Result<Boolean> applyAfterSale(@RequestBody CreateReturnGoodsAfterSaleCommand command);

    /**
     * 取消订单/超时未支付取消
     */
    @PostMapping("/cancelOrder")
    Result<Boolean> cancelOrder(@RequestBody CancelOrderCommand command);

    /**
     * 缺品
     */
    @PostMapping("/lackItem")
    Result<LackDTO> lackItem(@RequestBody LackCommand command);

    /**
     * 取消订单支付退款回调
     */
    @PostMapping("/refundCallback")
    Result<Boolean> refundCallback(@RequestBody RefundOrderCallbackCommand command);

    /**
     * 接收客服的审核结果
     */
    @PostMapping("/receiveCustomerAuditResult")
    Result<Boolean> receiveCustomerAuditResult(@RequestBody AfterSaleAuditCommand command);

    /**
     * 用户撤销售后申请
     */
    @PostMapping("/revokeAfterSale")
    Result<Boolean> revokeAfterSale(@RequestBody RevokeAfterSaleCommand command);
}
