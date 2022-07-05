package com.ruyuan.eshop.order.service.impl;

import com.ruyuan.eshop.order.dao.AfterSaleInfoDAO;
import com.ruyuan.eshop.order.dao.AfterSaleItemDAO;
import com.ruyuan.eshop.order.dao.AfterSaleRefundDAO;
import com.ruyuan.eshop.order.dao.OrderInfoDAO;
import com.ruyuan.eshop.order.domain.dto.OrderLackInfo;
import com.ruyuan.eshop.order.manager.OrderNoManager;
import com.ruyuan.eshop.order.service.amount.AfterSaleAmountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单缺品处理器
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class OrderLackProcessor {

    @Autowired
    private OrderNoManager orderNoManager;

    @Autowired
    private OrderInfoDAO orderInfoDAO;

    @Autowired
    private AfterSaleInfoDAO afterSaleInfoDAO;

    @Autowired
    private AfterSaleItemDAO afterSaleItemDAO;

    @Autowired
    private AfterSaleRefundDAO afterSaleRefundDAO;

    @Autowired
    private AfterSaleAmountService afterSaleAmountService;

    /**
     * 保存缺品数据
     *
     * @param orderLackInfo
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveLackInfo(OrderLackInfo orderLackInfo) {

        // 1、存储售后单,item和退款单;
        afterSaleInfoDAO.save(orderLackInfo.getLackAfterSaleOrder());
        afterSaleItemDAO.saveBatch(orderLackInfo.getAfterSaleItems());
        afterSaleRefundDAO.save(orderLackInfo.getAfterSaleRefund());
        // 2、更新订单扩展信息
        orderInfoDAO.updateOrderExtJson(orderLackInfo.getOrderId(), orderLackInfo.getLackExtJson());
    }
}
