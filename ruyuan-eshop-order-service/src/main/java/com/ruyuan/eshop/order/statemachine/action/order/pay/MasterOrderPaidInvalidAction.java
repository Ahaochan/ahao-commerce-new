package com.ruyuan.eshop.order.statemachine.action.order.pay;

import com.ruyuan.eshop.common.enums.OrderOperateTypeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.statemachine.action.OrderStateAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 主订单已支付失效Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class MasterOrderPaidInvalidAction extends OrderStateAction<OrderInfoDO> {

    @Override
    protected OrderInfoDTO onStateChangeInternal(OrderStatusChangeEnum event, OrderInfoDO context) {
        String orderId = context.getOrderId();
        Integer newPreOrderStatus = context.getOrderStatus();
        Integer currentOrderStatus = OrderStatusEnum.INVALID.getCode();

        // 1、将主订单状态设置为无效订单
        List<String> orderIdList = Collections.singletonList(orderId);
        updateOrderStatus(orderIdList, currentOrderStatus);

        // 2、新增订单状态变更日志
        Integer operateType = OrderOperateTypeEnum.ORDER_PAID_INVALID.getCode();
        String remark = "订单支付回调操作，主订单状态变更" + newPreOrderStatus + "-" + currentOrderStatus;
        saveOrderOperateLog(orderId, operateType, newPreOrderStatus, currentOrderStatus, remark);

        // 3、返回订单标准信息
        return orderConverter.orderInfoDO2DTO(context);
    }

    @Override
    public OrderStatusChangeEnum event() {
        return OrderStatusChangeEnum.ORDER_PAID_INVALID;
    }
}
