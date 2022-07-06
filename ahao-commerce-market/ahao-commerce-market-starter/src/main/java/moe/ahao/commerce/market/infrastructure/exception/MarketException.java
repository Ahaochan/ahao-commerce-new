package moe.ahao.commerce.market.infrastructure.exception;

import moe.ahao.exception.BizException;

/* package */ class MarketException extends BizException {
    public MarketException(int code, String message) {
        super(code, message);
    }
}
