package moe.ahao.commerce.order.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 快照类型
 */
@Getter
@AllArgsConstructor
public enum SnapshotTypeEnum {
    ORDER_COUPON(1, "订单优惠券信息"),
    ORDER_AMOUNT(2, "订单费用信息"),
    ORDER_ITEM(3, "订单条目信息"),
    ;
    private final Integer code;
    private final String name;
}
