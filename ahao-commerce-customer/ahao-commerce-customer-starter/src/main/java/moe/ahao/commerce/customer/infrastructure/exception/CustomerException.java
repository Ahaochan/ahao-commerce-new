package moe.ahao.commerce.customer.infrastructure.exception;

import moe.ahao.exception.BizException;

/* package */ class CustomerException extends BizException {
    public CustomerException(int code, String message) {
        super(code, message);
    }
}
