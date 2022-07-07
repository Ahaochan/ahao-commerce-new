package moe.ahao.commerce.wms.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ahao.exception.BizExceptionEnum;

@Getter
@AllArgsConstructor
public enum WmsExceptionEnum implements BizExceptionEnum<WmsException> {
    DELIVERY_ORDER_ID_GEN_ERROR(108001, "出库单ID生成异常"),
    TMS_IS_ERROR(108002, "tms系统异常"),
    EXCEPTION(108003, "捡货异常！");
    private final int code;
    private final String message;

    public WmsException msg(Object... args) {
        return new WmsException(code, String.format(message, args));
    }
}
