package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单操作类型枚举值
 */
@Getter
@AllArgsConstructor
public enum OrderOperateTypeEnum {
    NEW_ORDER(10, "新建订单"),
    MANUAL_CANCEL_ORDER(20, "手工取消订单"),
    AUTO_CANCEL_ORDER(30, "超时未支付自动取消订单"),
    PAID_ORDER(40, "完成订单支付"),
    PUSH_ORDER_FULFILL(50, "推送订单至履约"),
    ORDER_OUT_STOCK(60, "订单已出库"),
    ORDER_DELIVERED(70, "订单已配送"),
    ORDER_SIGNED(80, "订单已签收");
    private final Integer code;
    private final String name;
}
