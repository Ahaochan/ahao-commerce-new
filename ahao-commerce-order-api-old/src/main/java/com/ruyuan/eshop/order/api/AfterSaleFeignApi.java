package com.ruyuan.eshop.order.api;

import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.order.domain.dto.LackDTO;
import com.ruyuan.eshop.order.domain.request.CancelOrderRequest;
import com.ruyuan.eshop.order.domain.request.LackRequest;
import com.ruyuan.eshop.order.domain.request.RefundOrderCallbackCommand;
import com.ruyuan.eshop.order.domain.request.RevokeAfterSaleRequest;
import moe.ahao.commerce.customer.api.command.CustomerReceiveAfterSaleCommand;
import moe.ahao.commerce.customer.api.command.CustomerReviewReturnGoodsCommand;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 订单中心-逆向售后业务接口
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public interface AfterSaleFeignApi {
    String PATH = "/api/aftersale";

    /**
     * 取消订单/超时未支付取消
     */
    @PostMapping("/cancelOrder")
    JsonResult<Boolean> cancelOrder(CancelOrderRequest cancelOrderRequest);

    /**
     * 缺品
     */
    @PostMapping("/lackItem")
    JsonResult<LackDTO> lackItem(LackRequest request);

    /**
     * 取消订单支付退款回调
     */
    @PostMapping("/refundCallback")
    Result<Boolean> refundCallback(RefundOrderCallbackCommand command);

    /**
     * 接收客服的审核结果
     */
    @PostMapping("/receiveCustomerAuditResult")
    Result<Boolean> receiveCustomerAuditResult(CustomerReviewReturnGoodsCommand command);

    /**
     * 用户撤销售后申请
     */
    @PostMapping("/revokeAfterSale")
    JsonResult<Boolean> revokeAfterSale(RevokeAfterSaleRequest request);

    /**
     * 提供给客服系统查询售后支付单信息
     */
    @PostMapping("/customerFindAfterSaleRefundInfo")
    Result<String> customerFindAfterSaleRefundInfo(CustomerReceiveAfterSaleCommand command);
}
