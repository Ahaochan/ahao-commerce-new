package com.ruyuan.eshop.product.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 商品类型枚举
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum ProductTypeEnum {

    NORMAL(1, "一般商品"),
    VIRTUAL(2, "虚拟商品"),
    PRE_SALE(3, "预售商品"),
    UNKNOWN(127, "其他");


    private Integer code;
    private String msg;

    public static Map<Integer, String> toMap() {
        Map<Integer, String> map = new HashMap<>(16);
        for (ProductTypeEnum element : ProductTypeEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static ProductTypeEnum getByCode(Integer code) {
        for (ProductTypeEnum element : ProductTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

    public static Set<Integer> allowableValues() {
        Set<Integer> allowableValues = new HashSet<>(values().length);
        for (ProductTypeEnum orderTypeEnum : values()) {
            allowableValues.add(orderTypeEnum.getCode());
        }
        return allowableValues;
    }

}
