package moe.ahao.commerce.product.infrastructure.exception;

import moe.ahao.exception.BizException;

/* package */ class ProductException extends BizException {
    public ProductException(int code, String message) {
        super(code, message);
    }
}
