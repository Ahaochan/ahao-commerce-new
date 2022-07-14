package moe.ahao.commerce.fulfill.infrastructure.exception;

import moe.ahao.exception.BizException;

/* package */ class FulfillException extends BizException {
    public FulfillException(int code, String message) {
        super(code, message);
    }
}
