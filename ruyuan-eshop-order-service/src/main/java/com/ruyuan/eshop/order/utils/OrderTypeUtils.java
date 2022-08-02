package com.ruyuan.eshop.order.utils;

import com.ruyuan.eshop.common.enums.OrderTypeEnum;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.product.enums.ProductTypeEnum;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
public class OrderTypeUtils {

    public static void setOrderType(Integer productType, OrderInfoDO orderInfoDO) {
        ProductTypeEnum productTypeEnum = ProductTypeEnum.getByCode(productType);
        if (ProductTypeEnum.NORMAL.equals(productTypeEnum)) {
            orderInfoDO.setOrderType(OrderTypeEnum.NORMAL.getCode());
        } else if (ProductTypeEnum.VIRTUAL.equals(productTypeEnum)) {
            orderInfoDO.setOrderType(OrderTypeEnum.VIRTUAL.getCode());
        } else if (ProductTypeEnum.PRE_SALE.equals(productTypeEnum)) {
            orderInfoDO.setOrderType(OrderTypeEnum.PRE_SALE.getCode());
        }
    }

}
