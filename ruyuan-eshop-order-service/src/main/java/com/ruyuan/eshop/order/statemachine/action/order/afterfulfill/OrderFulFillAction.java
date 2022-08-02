package com.ruyuan.eshop.order.statemachine.action.order.afterfulfill;

import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单已履约Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class OrderFulFillAction extends AbstractAfterFulfillResultAction {

    @Override
    protected OrderStatusEnum handleStatus() {
        return OrderStatusEnum.PAID;
    }

    @Override
    public OrderStatusChangeEnum event() {
        return OrderStatusChangeEnum.ORDER_FULFILLED;
    }
}
