package moe.ahao.commerce.wms.infrastructure.exception;

import moe.ahao.exception.BizException;

/* package */ class WmsException extends BizException {
    public WmsException(int code, String message) {
        super(code, message);
    }
}
