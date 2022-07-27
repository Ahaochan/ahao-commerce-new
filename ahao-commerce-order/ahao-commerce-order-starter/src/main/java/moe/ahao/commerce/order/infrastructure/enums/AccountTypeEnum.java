package moe.ahao.commerce.order.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户枚举
 */
@Getter
@AllArgsConstructor
public enum AccountTypeEnum {
    THIRD(1, "第三方"),
    OTHER(127, "其他"),
    ;
    private final Integer code;
    private final String name;

    public static AccountTypeEnum getByCode(Integer code) {
        for (AccountTypeEnum element : AccountTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}
