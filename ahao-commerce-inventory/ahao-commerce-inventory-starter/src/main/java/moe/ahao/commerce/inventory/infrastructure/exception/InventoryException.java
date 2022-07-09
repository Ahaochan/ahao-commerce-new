package moe.ahao.commerce.inventory.infrastructure.exception;

import moe.ahao.exception.BizException;

/* package */ class InventoryException extends BizException {
    public InventoryException(int code, String message) {
        super(code, message);
    }
}
