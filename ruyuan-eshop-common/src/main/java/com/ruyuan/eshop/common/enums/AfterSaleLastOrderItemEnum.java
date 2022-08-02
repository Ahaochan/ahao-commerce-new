package com.ruyuan.eshop.common.enums;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 售后订单尾笔条目枚举
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public enum AfterSaleLastOrderItemEnum {

    NOT_LAST_ORDER_ITEM(10, "非尾笔订单条目"),
    LAST_ORDER_ITEM(20, "尾笔订单条目");


    private Integer code;

    private String msg;

    AfterSaleLastOrderItemEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static Map<Integer, String> toMap() {
        Map<Integer, String> map = new HashMap<>(16);
        for (AfterSaleLastOrderItemEnum element : AfterSaleLastOrderItemEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static AfterSaleLastOrderItemEnum getByCode(Integer code) {
        for (AfterSaleLastOrderItemEnum element : AfterSaleLastOrderItemEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

    public static Set<Integer> allowableValues() {
        Set<Integer> allowableValues = new HashSet<>(values().length);
        for (AfterSaleLastOrderItemEnum typeEnun : values()) {
            allowableValues.add(typeEnun.getCode());
        }
        return allowableValues;
    }
}