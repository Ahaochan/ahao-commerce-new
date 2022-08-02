package com.ruyuan.eshop.order.service;

import com.ruyuan.eshop.market.domain.request.ReleaseUserCouponRequest;
import com.ruyuan.eshop.order.domain.dto.SendLackItemRefundEventDTO;
import com.ruyuan.eshop.order.domain.dto.SendOrderStdEventDTO;
import com.ruyuan.eshop.order.domain.request.*;

/**
 * RocketMQ发送消息服务
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public interface RocketMqService {

    /**
     * 发送订单超时未支付消息
     *
     * @param orderPaymentDelayRequest 超时未支付消息
     */
    void sendOrderPayTimeoutDelayMessage(OrderPaymentDelayRequest orderPaymentDelayRequest);

    /**
     * 发送订单标准状态变更消息
     *
     * @param sendOrderStdEventDTO 发送订单标准变更消息请求
     */
    void sendStandardOrderStatusChangeMessage(SendOrderStdEventDTO sendOrderStdEventDTO);

    /**
     * 发送缺品退款消息
     */
    void sendLackItemRefundMessage(SendLackItemRefundEventDTO sendLackItemRefundEventDTO);

    /**
     * 取消订单向下游发送释放权益资产消息
     *
     * @param cancelOrderAssembleRequest 取消订单信息
     */
    void sendReleaseAssetsMessage(CancelOrderAssembleRequest cancelOrderAssembleRequest);

    /**
     * 取消订单发送实际退款消息
     *
     * @param cancelOrderAssembleRequest 取消订单信息
     */
    void sendCancelOrderRefundMessage(CancelOrderAssembleRequest cancelOrderAssembleRequest);

    /**
     * 手动售后发送实际退款消息
     *
     * @param manualAfterSaleDTO 售后信息
     */
    void sendAfterSaleRefundMessage(ManualAfterSaleDTO manualAfterSaleDTO);

    /**
     * 售后审核通过发送释放权益资产消息
     *
     * @param auditPassReleaseAssetsRequest 审核通过释放权益资产信息
     */
    void sendAuditPassMessage(AuditPassReleaseAssetsRequest auditPassReleaseAssetsRequest);

    /**
     * 手动售后释放优惠券消息
     *
     * @param releaseUserCouponRequest 释放优惠券信息
     */
    void sendAfterSaleReleaseCouponMessage(ReleaseUserCouponRequest releaseUserCouponRequest);
}
