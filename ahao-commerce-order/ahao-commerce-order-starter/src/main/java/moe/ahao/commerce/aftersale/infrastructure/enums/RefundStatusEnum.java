package moe.ahao.commerce.aftersale.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
@AllArgsConstructor
public enum RefundStatusEnum {
    UN_REFUND(10, "未退款"),
    REFUND_SUCCESS(20, "退款成功"),
    REFUND_FAIL(30, "退款失败"),
    ;
    private final int code;
    private final String name;
}
