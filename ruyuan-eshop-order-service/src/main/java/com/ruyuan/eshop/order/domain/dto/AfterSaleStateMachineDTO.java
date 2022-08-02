package com.ruyuan.eshop.order.domain.dto;

import com.ruyuan.eshop.common.message.RefundMessage;
import com.ruyuan.eshop.customer.domain.request.CustomerReviewReturnGoodsRequest;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.domain.request.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 逆向售后状态机DTO
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class AfterSaleStateMachineDTO implements Serializable {
    private static final long serialVersionUID = -9090680458265572922L;

    /**
     * 手动售后DTO
     */
    private ManualAfterSaleDTO manualAfterSaleDTO;

    /**
     * 售后单DO
     */
    private AfterSaleInfoDO afterSaleInfoDO;

    /**
     * 支付退款回调参数
     */
    private RefundCallbackRequest refundCallbackRequest;

    /**
     * 取消订单组装参数
     */
    private CancelOrderAssembleRequest cancelOrderAssembleRequest;

    /**
     * 手动售后组装参数
     */
    private ReturnGoodsOrderRequest returnGoodsOrderRequest;

    /**
     * 客服审核售后参数
     */
    private CustomerReviewReturnGoodsRequest customerReviewReturnGoodsRequest;

    /**
     * 执行实际退款时的MQ退款消息体
     */
    private RefundMessage refundMessage;

    /**
     * 撤销售后组装参数
     */
    private RevokeAfterSaleRequest revokeAfterSaleRequest;
}
