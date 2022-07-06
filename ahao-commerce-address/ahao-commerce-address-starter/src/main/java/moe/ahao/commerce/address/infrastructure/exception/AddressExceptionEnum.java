package moe.ahao.commerce.address.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ahao.exception.BizExceptionEnum;

@Getter
@AllArgsConstructor
public enum AddressExceptionEnum implements BizExceptionEnum<AddressException> {
    ADDRESS_NOT_FOUND(100001, "查询地址失败，入参name:[%s]和code:[%s]查询不到数据"),
    ADDRESS_MULTI_RESULT(100002, "查询地址失败，入参name:[%s]和code:[%s]查询出多条数据"),
    ADDRESS_NOT_MATCH(100003, "查询地址失败，入参name:[%s]和code:[%s]与查询出来的数据name:[%s]和code:[%s]不匹配"),
    ;

    private final int code;
    private final String message;

    public AddressException msg(Object... args) {
        return new AddressException(code, String.format(message, args));
    }
}
