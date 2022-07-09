package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品类型枚举
 */
@Getter
@AllArgsConstructor
public enum ProductTypeEnum {
    NORMAL_PRODUCT(1, "普通商品"),
    ADVANCE_SALE(2, "预售商品"),
    ;
    private final Integer code;
    private final String name;
}
