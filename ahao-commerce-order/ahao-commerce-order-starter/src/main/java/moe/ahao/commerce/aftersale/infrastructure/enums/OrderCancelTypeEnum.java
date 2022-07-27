package moe.ahao.commerce.aftersale.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单取消类型枚举
 */
@Getter
@AllArgsConstructor
public enum OrderCancelTypeEnum {
    USER_CANCELED(0, "用户手动取消"),
    TIMEOUT_CANCELED(1, "超时未支付自动取消"),
    CUSTOMER_CANCELED(2, "用户授权客服取消"),
    ;
    private final int code;
    private final String name;

    public static OrderCancelTypeEnum getByCode(Integer code) {
        for (OrderCancelTypeEnum element : OrderCancelTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}
