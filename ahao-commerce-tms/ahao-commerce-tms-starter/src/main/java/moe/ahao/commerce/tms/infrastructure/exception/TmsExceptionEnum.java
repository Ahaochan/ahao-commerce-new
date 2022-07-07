package moe.ahao.commerce.tms.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ahao.exception.BizExceptionEnum;

@Getter
@AllArgsConstructor
public enum TmsExceptionEnum implements BizExceptionEnum<TmsException> {
    EXCEPTION(900001, "发货异常！")
    ;

    private final int code;
    private final String message;

    public TmsException msg(Object... args) {
        return new TmsException(code, String.format(message, args));
    }
}
