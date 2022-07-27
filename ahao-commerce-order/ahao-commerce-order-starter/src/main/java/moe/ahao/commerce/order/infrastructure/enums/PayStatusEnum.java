package moe.ahao.commerce.order.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum PayStatusEnum {
    UNPAID(10, "未支付"),
    PAID(20, "已支付"),
    ;
    private final Integer code;
    private final String name;
}
