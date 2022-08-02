package com.ruyuan.eshop.order.statemachine.action.order.cancel;

import com.ruyuan.eshop.common.enums.OrderOperateTypeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.order.domain.entity.OrderOperateLogDO;
import org.springframework.stereotype.Component;

/**
 * 订单未支付超时自动取消Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class OrderUnPaidAutoTimeoutCancelAction extends AbstractOrderCancelAction {

    @Override
    protected void setOperateTypeAndRemark(OrderOperateLogDO operateLogDO, Integer cancelType) {
        operateLogDO.setOperateType(OrderOperateTypeEnum.AUTO_CANCEL_ORDER.getCode());
        operateLogDO.setRemark(OrderOperateTypeEnum.AUTO_CANCEL_ORDER.getMsg()
                + operateLogDO.getPreStatus() + "-" + operateLogDO.getCurrentStatus());
    }

    @Override
    public OrderStatusChangeEnum event() {
        return OrderStatusChangeEnum.ORDER_UN_PAID_AUTO_TIMEOUT_CANCELLED;
    }
}
