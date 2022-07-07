package moe.ahao.commerce.tms.infrastructure.exception;

import moe.ahao.exception.BizException;

/* package */ class TmsException extends BizException {
    public TmsException(int code, String message) {
        super(code, message);
    }
}
