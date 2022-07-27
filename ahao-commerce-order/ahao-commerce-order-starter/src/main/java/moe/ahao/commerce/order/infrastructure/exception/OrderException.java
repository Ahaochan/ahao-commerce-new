package moe.ahao.commerce.order.infrastructure.exception;

import moe.ahao.exception.BizException;

public class OrderException extends BizException {
    public OrderException(int code, String message) {
        super(code, message);
    }
}
