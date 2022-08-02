package com.ruyuan.eshop.order.domain.request;

import com.ruyuan.eshop.order.enums.OrderQueryDataTypeEnums;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单详情请求
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class OrderDetailRequest implements Serializable {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单项查询枚举
     */
    private OrderQueryDataTypeEnums[] queryDataTypes;
}
