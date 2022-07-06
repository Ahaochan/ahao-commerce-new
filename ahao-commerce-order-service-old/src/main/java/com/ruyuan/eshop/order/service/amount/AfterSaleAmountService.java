package com.ruyuan.eshop.order.service.amount;

import com.ruyuan.eshop.order.dao.OrderItemDAO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.domain.entity.OrderItemDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 售后金额计算service
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class AfterSaleAmountService {

    @Autowired
    private OrderItemDAO orderItemDAO;

    /**
     * 计算订单条目缺品实际退款金额
     * 计算公式：（缺品数量/下单数量） * 原付款金额
     *
     * @param orderItem
     * @param lackNum
     * @return
     */
    public BigDecimal calculateOrderItemLackRealRefundAmount(OrderItemDO orderItem, BigDecimal lackNum) {
        BigDecimal rate = lackNum.divide(orderItem.getSaleQuantity(), 2, RoundingMode.HALF_UP);
        //金额向上取整
        BigDecimal itemRefundAmount = orderItem.getPayAmount().multiply(rate).setScale(2, RoundingMode.DOWN);
        return itemRefundAmount;
    }

    /**
     * 计算订单总申请退款金额
     *
     * @param lackItems
     * @return
     */
    public BigDecimal calculateOrderLackApplyRefundAmount(List<AfterSaleItemDO> lackItems) {

        BigDecimal applyRefundAmount = BigDecimal.ZERO;

        for (AfterSaleItemDO item : lackItems) {
            applyRefundAmount = applyRefundAmount.add(item.getApplyRefundAmount());
        }

        return applyRefundAmount;
    }

    /**
     * 计算订单总实际退款金额
     *
     * @param lackItems
     * @return
     */
    public BigDecimal calculateOrderLackRealRefundAmount(List<AfterSaleItemDO> lackItems) {

        BigDecimal realRefundAmount = BigDecimal.ZERO;

        for (AfterSaleItemDO item : lackItems) {
            realRefundAmount = realRefundAmount.add(item.getRealRefundAmount());
        }

        return realRefundAmount;
    }

}
