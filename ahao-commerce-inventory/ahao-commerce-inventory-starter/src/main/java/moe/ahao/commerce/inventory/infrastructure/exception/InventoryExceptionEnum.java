package moe.ahao.commerce.inventory.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ahao.exception.BizExceptionEnum;

@Getter
@AllArgsConstructor
public enum InventoryExceptionEnum implements BizExceptionEnum<InventoryException> {
    PRODUCT_SKU_STOCK_NOT_FOUND_ERROR(100001, "商品库存记录不存在"),
    PRODUCT_SKU_STOCK_EXISTED_ERROR(100002, "商品库存记录已存在"),
    DEDUCT_PRODUCT_SKU_STOCK_ERROR(100003, "扣减商品库存失败"),
    DEDUCT_PRODUCT_SKU_STOCK_CANNOT_ACQUIRE(100003, "无法获取扣减库存锁"),
    RELEASE_PRODUCT_SKU_STOCK_ERROR(100004, "释放商品库存失败"),
    RELEASE_PRODUCT_SKU_STOCK_LOCK_CANNOT_ACQUIRE(100004, "无法获取释放库存锁"),
    CONSUME_MQ_FAILED(100005, "消费MQ消息失败"),
    SKU_CODE_IS_EMPTY(100006, "skuCode不能为空"),
    SALE_STOCK_QUANTITY_IS_EMPTY(100007, "销售库存数量不能为空"),
    SALE_STOCK_QUANTITY_CANNOT_BE_NEGATIVE_NUMBER(100008, "销售库存数量不能小于0"),
    ADD_PRODUCT_SKU_STOCK_ERROR(100009, "添加商品库存异常"),
    SALE_STOCK_INCREMENTAL_QUANTITY_IS_EMPTY(100010, "销售库存增量不能为空"),
    SALE_STOCK_INCREMENTAL_QUANTITY_CANNOT_BE_ZERO(100011, "销售库存增量不能为0"),
    INCREASE_PRODUCT_SKU_STOCK_ERROR(100012, "调整商品库存异常"),
    ;

    private final int code;
    private final String message;
    public InventoryException msg(Object... args) {
        return new InventoryException(code, String.format(message, args));
    }
}
