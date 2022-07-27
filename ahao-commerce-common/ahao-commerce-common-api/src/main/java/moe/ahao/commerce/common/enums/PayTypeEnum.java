package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式
 */
@Getter
@AllArgsConstructor
public enum PayTypeEnum {
    ALIPAY(10, "支付宝"),
    WEIXIN_PAY(20, "微信支付"),
    ;
    private final Integer code;
    private final String name;

    public static PayTypeEnum getByCode(Integer code) {
        for (PayTypeEnum element : PayTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}
