package com.ruyuan.eshop.order.service;

import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.message.RefundMessage;
import com.ruyuan.eshop.order.domain.request.*;


/**
 * 订单逆向售后业务接口
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public interface OrderAfterSaleService {

    /**
     * 取消订单/超时未支付取消 入口
     * <p>
     * 接收3处调用：
     * 1、用户手动取消调用
     * 2、下单后订单流入延迟MQ,30分钟后触发调用
     * 3、XXL-JOB定时任务扫描触发调用
     */
    JsonResult<Boolean> cancelOrder(CancelOrderRequest cancelOrderRequest);

    /**
     * 取消订单/超时未支付取消 执行 退款前计算金额、记录售后信息等准备工作
     */
    JsonResult<Boolean> processCancelOrder(CancelOrderAssembleRequest cancelOrderAssembleRequest);

    /**
     * 执行退款
     */
    JsonResult<Boolean> refundMoney(RefundMessage refundMessage);

    /**
     * 支付退款回调 入口
     */
    JsonResult<Boolean> receivePaymentRefundCallback(RefundCallbackRequest payRefundCallbackRequest);

    /**
     * 处理售后申请 入口
     */
    JsonResult<Boolean> processApplyAfterSale(ReturnGoodsOrderRequest returnGoodsOrderRequest);

    /**
     * 发送退款短信
     */
    JsonResult<Boolean> sendRefundMobileMessage(String orderId);

    /**
     * 发送APP通知
     */
    JsonResult<Boolean> sendRefundAppMessage(String orderId);

    /**
     * 撤销售后申请
     */
    void revokeAfterSale(RevokeAfterSaleRequest revokeAfterSaleRequest);

    /**
     * 正向生单后的执行取消订单前验证 入口
     */
    void verifyBeforeOrderCancellation(String orderId);
}
