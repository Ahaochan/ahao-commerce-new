package moe.ahao.commerce.order.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配送方式枚举
 */
@Getter
@AllArgsConstructor
public enum DeliveryTypeEnum {
    NULL(0, "无配送方式"),
    SELF(1, "自主配送");
    private final Integer code;
    private final String name;

    public static DeliveryTypeEnum getByCode(Integer code) {
        for (DeliveryTypeEnum element : DeliveryTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}
