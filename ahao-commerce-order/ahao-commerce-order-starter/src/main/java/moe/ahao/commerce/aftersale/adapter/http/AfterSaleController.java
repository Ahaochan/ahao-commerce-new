package moe.ahao.commerce.aftersale.adapter.http;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.AfterSaleFeignApi;
import moe.ahao.commerce.aftersale.api.command.*;
import moe.ahao.commerce.aftersale.api.dto.LackDTO;
import moe.ahao.commerce.aftersale.application.*;
import moe.ahao.commerce.order.application.RefundCallbackAppService;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单中心-逆向售后业务接口
 */
@Slf4j
@RestController
@RequestMapping(AfterSaleFeignApi.PATH)
public class AfterSaleController implements AfterSaleFeignApi {
    @Autowired
    private ReturnGoodsAfterSaleAppService returnGoodsAfterSaleAppService;
    @Autowired
    private CancelOrderAppService cancelOrderAppService;
    @Autowired
    private OrderLackAppService orderLackAppService;
    @Autowired
    private RefundCallbackAppService refundCallbackAppService;
    @Autowired
    private AfterSaleAuditAppService afterSaleAuditAppService;
    @Autowired
    private RevokeAfterSaleAppService revokeAfterSaleAppService;

    @Override
    public Result<Boolean> applyAfterSale(@RequestBody CreateReturnGoodsAfterSaleCommand command) {
        returnGoodsAfterSaleAppService.create(command);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> cancelOrder(@RequestBody CancelOrderCommand command) {
        boolean result = cancelOrderAppService.cancel(command);
        return Result.success(result);
    }

    @Override
    public Result<LackDTO> lackItem(@RequestBody LackCommand command) {
        LackDTO lackDTO = orderLackAppService.execute(command);
        return Result.success(lackDTO);
    }

    @Override
    public Result<Boolean> refundCallback(@RequestBody RefundOrderCallbackCommand command) {
        refundCallbackAppService.refundCallback(command);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> receiveCustomerAuditResult(@RequestBody AfterSaleAuditCommand command) {
        afterSaleAuditAppService.audit(command);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> revokeAfterSale(@RequestBody RevokeAfterSaleCommand command) {
        revokeAfterSaleAppService.revoke(command);
        return Result.success(true);
    }
}
