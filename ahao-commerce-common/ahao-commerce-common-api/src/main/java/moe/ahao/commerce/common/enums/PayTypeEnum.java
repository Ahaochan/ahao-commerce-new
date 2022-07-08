package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式
 */
@Getter
@AllArgsConstructor
public enum PayTypeEnum {
    ALIPAY(1, "支付宝"),
    WEIXIN_PAY(2, "微信支付"),
    ;
    private final Integer code;
    private final String name;
}
