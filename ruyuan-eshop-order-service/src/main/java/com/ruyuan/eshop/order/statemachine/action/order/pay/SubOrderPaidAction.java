package com.ruyuan.eshop.order.statemachine.action.order.pay;

import com.google.common.collect.Lists;
import com.ruyuan.eshop.common.enums.OrderOperateTypeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import com.ruyuan.eshop.order.dao.OrderOperateLogDAO;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.domain.entity.OrderOperateLogDO;
import com.ruyuan.eshop.order.domain.request.SubOrderPaidRequest;
import com.ruyuan.eshop.order.enums.PayStatusEnum;
import com.ruyuan.eshop.order.statemachine.action.OrderStateAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 子订单已支付Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class SubOrderPaidAction extends OrderStateAction<SubOrderPaidRequest> {

    @Autowired
    private OrderOperateLogDAO orderOperateLogDAO;

    @Override
    protected OrderInfoDTO onStateChangeInternal(OrderStatusChangeEnum event, SubOrderPaidRequest context) {
        Date payTime = context.getPayTime();
        OrderInfoDO subOrder = context.getSubOrder();

        // 1、更新子订单的状态
        Integer subCurrentOrderStatus = OrderStatusEnum.PAID.getCode();
        List<String> subOrderIdList = Lists.newArrayList(subOrder.getOrderId());

        // 2、更新子订单状态和支付时间
        updateOrderStatusAndPayTime(subOrderIdList, subCurrentOrderStatus, payTime);

        // 3、更新子订单的支付明细
        updatePaymentStatusAndPayTime(subOrderIdList, PayStatusEnum.PAID.getCode(), payTime);

        // 4、保存子订单操作日志
        orderOperateLogDAO.save(buildSubOperateLog(subOrder, subCurrentOrderStatus));

        // 5、返回订单标准信息
        return orderConverter.orderInfoDO2DTO(subOrder);
    }

    private OrderOperateLogDO buildSubOperateLog(OrderInfoDO subOrder, Integer subCurrentOrderStatus) {
        String subOrderId = subOrder.getOrderId();
        Integer subPreOrderStatus = subOrder.getOrderStatus();
        // 订单状态变更日志
        OrderOperateLogDO subOrderOperateLogDO = new OrderOperateLogDO();
        subOrderOperateLogDO.setOrderId(subOrderId);
        subOrderOperateLogDO.setOperateType(OrderOperateTypeEnum.PAID_ORDER.getCode());
        subOrderOperateLogDO.setPreStatus(subPreOrderStatus);
        subOrderOperateLogDO.setCurrentStatus(subCurrentOrderStatus);
        subOrderOperateLogDO.setRemark("订单支付回调操作，子订单状态变更"
                + subOrderOperateLogDO.getPreStatus() + "-"
                + subOrderOperateLogDO.getCurrentStatus());
        return subOrderOperateLogDO;
    }

    @Override
    public OrderStatusChangeEnum event() {
        return OrderStatusChangeEnum.SUB_ORDER_PAID;
    }


}
