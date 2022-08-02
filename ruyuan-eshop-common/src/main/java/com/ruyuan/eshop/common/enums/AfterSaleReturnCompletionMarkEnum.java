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
public enum AfterSaleReturnCompletionMarkEnum {
    NOT_ALL_RETURN_GOODS(10, "购买的sku未全部退货"),
    ALL_RETURN_GOODS(20, "购买的sku已全部退货");


    private Integer code;

    private String msg;

    AfterSaleReturnCompletionMarkEnum(Integer code, String msg) {
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
        for (AfterSaleReturnCompletionMarkEnum element : AfterSaleReturnCompletionMarkEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static AfterSaleReturnCompletionMarkEnum getByCode(Integer code) {
        for (AfterSaleReturnCompletionMarkEnum element : AfterSaleReturnCompletionMarkEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

    public static Set<Integer> allowableValues() {
        Set<Integer> allowableValues = new HashSet<>(values().length);
        for (AfterSaleReturnCompletionMarkEnum typeEnun : values()) {
            allowableValues.add(typeEnun.getCode());
        }
        return allowableValues;
    }
}