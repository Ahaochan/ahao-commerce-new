package moe.ahao.commerce.order.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单取消类型枚举
 */
@Getter
@AllArgsConstructor
public enum ReturnGoodsTypeEnum {
    AFTER_SALE_RETURN_GOODS(1, "售后退货"),
    ;
    private final Integer code;
    private final String name;
}
