package com.ruyuan.eshop.fulfill.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 履约单类型枚举
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum OrderFulfillTypeEnum {

    NORMAL(1, "一般履约单"),
    PRE_SALE(2, "预售履约单"),
    UNKNOWN(127, "其他");


    private Integer code;
    private String msg;

    public static Map<Integer, String> toMap() {
        Map<Integer, String> map = new HashMap<>(16);
        for (OrderFulfillTypeEnum element : OrderFulfillTypeEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static OrderFulfillTypeEnum getByCode(Integer code) {
        for (OrderFulfillTypeEnum element : OrderFulfillTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

    public static Set<Integer> allowableValues() {
        Set<Integer> allowableValues = new HashSet<>(values().length);
        for (OrderFulfillTypeEnum orderTypeEnum : values()) {
            allowableValues.add(orderTypeEnum.getCode());
        }
        return allowableValues;
    }

    /**
     * 立即进行履约调度的类型
     *
     * @return
     */
    public static Set<Integer> immediatelyFulfillTScheduleTypes() {
        Set<Integer> canFulfillTypes = new HashSet<>(values().length);
        canFulfillTypes.add(NORMAL.code);
        return canFulfillTypes;
    }

}