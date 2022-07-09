package moe.ahao.commerce.inventory.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存扣减日志状态
 */
@Getter
@AllArgsConstructor
public enum StockLogStatusEnum {
    DEDUCTED(1, "已扣除"),
    RELEASED(2, "已释放"),
    ;
    private final Integer code;
    private final String msg;
}
