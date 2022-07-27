package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 费用类型枚举
 */
@Getter
@AllArgsConstructor
public enum AmountTypeEnum {
    ORIGIN_PAY_AMOUNT(10, "订单支付原价"),
    COUPON_DISCOUNT_AMOUNT(20, "优惠券抵扣金额"),
    SHIPPING_AMOUNT(30, "运费"),
    BOX_AMOUNT(40, "包装费"),
    REAL_PAY_AMOUNT(50, "实付金额"),
    OTHER_AMOUNT(127, "其他费用");

    private final Integer code;
    private final String name;

    public static AmountTypeEnum getByCode(Integer code) {
        for (AmountTypeEnum element : AmountTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}
