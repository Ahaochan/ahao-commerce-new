package com.ruyuan.eshop.fulfill.utils;

import com.ruyuan.eshop.common.enums.OrderTypeEnum;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillTypeEnum;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
public class OrderFulfillTypeUtils {

    public static void setOrderFulfillType(Integer orderType, OrderFulfillDO orderFulfill) {
        OrderTypeEnum orderTypeEnum = OrderTypeEnum.getByCode(orderType);
        if (OrderTypeEnum.NORMAL.equals(orderTypeEnum)) {
            orderFulfill.setOrderFulfillType(OrderFulfillTypeEnum.NORMAL.getCode());
        } else if (OrderTypeEnum.PRE_SALE.equals(orderTypeEnum)) {
            orderFulfill.setOrderFulfillType(OrderFulfillTypeEnum.PRE_SALE.getCode());
        } else {
            orderFulfill.setOrderFulfillType(OrderFulfillTypeEnum.NORMAL.getCode());
        }
    }

}
