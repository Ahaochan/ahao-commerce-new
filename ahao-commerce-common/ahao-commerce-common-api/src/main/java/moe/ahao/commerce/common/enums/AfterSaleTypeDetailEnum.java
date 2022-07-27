package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后类型详情枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleTypeDetailEnum {
    ALL_REFUND(1, "售后全额退款"),
    TIMEOUT_NO_PAY(2, "超时未支付"),
    USER_CANCEL(3, "用户自主取消"),
    CUSTOMER_CANCEL(4, "授权客服取消"),
    PART_REFUND(5, "售后退货退款"),
    LACK_REFUND(6, "缺品退款"),
    ;
    private final Integer code;
    private final String name;
}
