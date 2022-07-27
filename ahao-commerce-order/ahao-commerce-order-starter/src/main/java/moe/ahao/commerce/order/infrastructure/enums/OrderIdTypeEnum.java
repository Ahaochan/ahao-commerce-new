package moe.ahao.commerce.order.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单号类型枚举
 */
@Getter
@AllArgsConstructor
public enum OrderIdTypeEnum {
    SALE_ORDER(10, "正向订单号"),
    AFTER_SALE(20, "售后单号"),
    ;
    private final Integer code;
    private final String name;

    public static OrderIdTypeEnum getByCode(Integer code) {
        for (OrderIdTypeEnum element : OrderIdTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}
