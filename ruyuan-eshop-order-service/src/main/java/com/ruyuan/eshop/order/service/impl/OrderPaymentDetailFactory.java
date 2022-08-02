package com.ruyuan.eshop.order.service.impl;

import com.ruyuan.eshop.common.utils.RandomUtil;
import com.ruyuan.eshop.order.dao.OrderPaymentDetailDAO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleRefundDO;
import com.ruyuan.eshop.order.domain.entity.OrderPaymentDetailDO;
import com.ruyuan.eshop.order.enums.AccountTypeEnum;
import com.ruyuan.eshop.order.enums.RefundStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单支付详情信息工厂
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class OrderPaymentDetailFactory {

    @Autowired
    private OrderPaymentDetailDAO orderPaymentDetailDAO;

    /**
     * 获取售后退款信息
     *
     * @param orderId     订单id
     * @param afterSaleId 售后id
     * @return
     */
    public AfterSaleRefundDO get(String orderId, String afterSaleId) {
        AfterSaleRefundDO afterSaleRefundDO = new AfterSaleRefundDO();
        afterSaleRefundDO.setAfterSaleId(afterSaleId);
        afterSaleRefundDO.setOrderId(orderId);
        afterSaleRefundDO.setAccountType(AccountTypeEnum.THIRD.getCode());
        afterSaleRefundDO.setRefundStatus(RefundStatusEnum.UN_REFUND.getCode());
        afterSaleRefundDO.setRemark(RefundStatusEnum.UN_REFUND.getMsg());
        afterSaleRefundDO.setAfterSaleBatchNo(orderId + RandomUtil.genRandomNumber(10));

        OrderPaymentDetailDO paymentDetail = orderPaymentDetailDAO.getPaymentDetailByOrderId(orderId);
        if (paymentDetail != null) {
            afterSaleRefundDO.setOutTradeNo(paymentDetail.getOutTradeNo());
            afterSaleRefundDO.setPayType(paymentDetail.getPayType());
        }

        return afterSaleRefundDO;
    }
}
