package com.ruyuan.eshop.order.statemachine.action;

import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class OrderActionFactory {

    @Resource
    private List<OrderStateAction<?>> actions;

    public OrderStateAction<?> getAction(OrderStatusChangeEnum event) {
        for (OrderStateAction<?> action : actions) {
            if (action.event() == null) {
                throw new IllegalArgumentException("event 返回值不能为空：" + action.getClass().getSimpleName());
            }
            if (action.event().equals(event)) {
                return action;
            }
        }
        return null;
    }
}
