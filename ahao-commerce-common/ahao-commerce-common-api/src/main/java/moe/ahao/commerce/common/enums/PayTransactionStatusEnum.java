package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付交易状态
 */
@Getter
@AllArgsConstructor
public enum PayTransactionStatusEnum {
    UNPAYED(1, "未付款"),
    SUCCESS(2, "支付成功"),
    FAILURE(3, "支付失败"),
    CLOSED(4, "支付交易关闭"),
    REFUND(5, "支付退款"),
    ;
    private final Integer code;
    private final String name;
}
