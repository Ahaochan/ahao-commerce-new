package moe.ahao.commerce.address.infrastructure.exception;

import moe.ahao.exception.BizException;

/* package */ class AddressException extends BizException {
    public AddressException(int code, String message) {
        super(code, message);
    }
}
