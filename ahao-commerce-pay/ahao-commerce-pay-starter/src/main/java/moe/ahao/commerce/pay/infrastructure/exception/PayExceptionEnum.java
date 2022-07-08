package moe.ahao.commerce.pay.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ahao.exception.BizExceptionEnum;


@Getter
@AllArgsConstructor
public enum PayExceptionEnum implements BizExceptionEnum<PayException> {
    PAY_REFUND_FAILED(600001, "支付退款接口调用失败"),
    ;

    private final int code;
    private final String message;

    public PayException msg(Object... args) {
        return new PayException(code, String.format(message, args));
    }
}
