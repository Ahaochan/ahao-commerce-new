package moe.ahao.commerce.order.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * 订单类型枚举
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {
    NORMAL(1, "一般订单"),
    UNKNOWN(127, "其他"),
    ;
    private final Integer code;
    private final String name;

    public static OrderTypeEnum getByCode(Integer code) {
        for (OrderTypeEnum element : OrderTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

    public static Set<Integer> allowableValues() {
        Set<Integer> allowableValues = new HashSet<>(values().length);
        for (OrderTypeEnum orderTypeEnum : values()) {
            allowableValues.add(orderTypeEnum.getCode());
        }
        return allowableValues;
    }
}
