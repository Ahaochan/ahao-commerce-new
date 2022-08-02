package com.ruyuan.eshop.order.statemachine.action.order.cancel;

import com.ruyuan.eshop.common.enums.OrderOperateTypeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.order.domain.entity.OrderOperateLogDO;
import org.springframework.stereotype.Component;

/**
 * 订单已支付手动取消Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class OrderPaidManualCancelAction extends AbstractOrderCancelAction {

    @Override
    protected void setOperateTypeAndRemark(OrderOperateLogDO operateLogDO, Integer cancelType) {
        operateLogDO.setOperateType(OrderOperateTypeEnum.MANUAL_CANCEL_ORDER.getCode());
        operateLogDO.setRemark(OrderOperateTypeEnum.MANUAL_CANCEL_ORDER.getMsg()
                + operateLogDO.getPreStatus() + "-" + operateLogDO.getCurrentStatus());
    }

    @Override
    public OrderStatusChangeEnum event() {
        return OrderStatusChangeEnum.ORDER_PAID_MANUAL_CANCELLED;
    }
}
