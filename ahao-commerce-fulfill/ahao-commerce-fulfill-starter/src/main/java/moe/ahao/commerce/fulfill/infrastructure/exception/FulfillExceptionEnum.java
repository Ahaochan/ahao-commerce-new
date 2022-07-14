package moe.ahao.commerce.fulfill.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ahao.exception.BizExceptionEnum;

@Getter
@AllArgsConstructor
public enum FulfillExceptionEnum implements BizExceptionEnum<FulfillException> {
    FULFILL_ID_GEN_ERROR(107001, "履约单ID生成异常"),
    WMS_IS_ERROR(107002, "调用WMS系统异常, %s"),
    TMS_IS_ERROR(107003, "调用TMS系统异常, %s"),
    ORDER_FULFILL_IS_ERROR(107004, "履约流程执行异常"),
    SEND_MQ_FAILED(107005, "发送MQ消息失败, %s"),
    ORDER_FULFILL_ERROR(107006, "订单履约错误"),
    ;

    private final int code;
    private final String message;

    public FulfillException msg(Object... args) {
        return new FulfillException(code, String.format(message, args));
    }
}
