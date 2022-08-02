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
 * 虚拟订单已签收Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class VirtualOrderSignedAction extends OrderStateAction<OrderInfoDO> {

    @Override
    protected OrderInfoDTO onStateChangeInternal(OrderStatusChangeEnum event, OrderInfoDO context) {
        // 更新虚拟订单支付成功：20->60
        String orderId = context.getOrderId();
        // 1、更新订单状态
        Integer preOrderStatus = OrderStatusEnum.PAID.getCode();
        Integer currentStatus = OrderStatusEnum.SIGNED.getCode();
        List<String> orderIdList = Collections.singletonList(orderId);
        updateOrderStatus(orderIdList, currentStatus);

        // 2、新增订单状态变更日志
        Integer operateType = OrderOperateTypeEnum.ORDER_SIGNED.getCode();
        String remark = "虚拟订单已签收" + preOrderStatus + "-" + currentStatus;
        saveOrderOperateLog(orderId, operateType, preOrderStatus, currentStatus, remark);

        // 3、返回订单信息
        return orderConverter.orderInfoDO2DTO(context);
    }

    @Override
    public OrderStatusChangeEnum event() {
        return OrderStatusChangeEnum.VIRTUAL_ORDER_SIGNED;
    }
}
