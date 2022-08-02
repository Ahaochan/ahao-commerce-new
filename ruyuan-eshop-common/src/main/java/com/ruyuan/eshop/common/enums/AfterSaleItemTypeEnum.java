package com.ruyuan.eshop.common.enums;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 售后条目类型枚举
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public enum AfterSaleItemTypeEnum {

    AFTER_SALE_ORDER_ITEM(10, "售后订单条目"),
    AFTER_SALE_COUPON(20, "尾笔条目退优惠券"),
    AFTER_SALE_FREIGHT(30, "尾笔条目退运费");

    private Integer code;

    private String msg;

    AfterSaleItemTypeEnum(Integer code, String msg) {
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
        for (AfterSaleItemTypeEnum element : AfterSaleItemTypeEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static AfterSaleItemTypeEnum getByCode(Integer code) {
        for (AfterSaleItemTypeEnum element : AfterSaleItemTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

    public static Set<Integer> allowableValues() {
        Set<Integer> allowableValues = new HashSet<>(values().length);
        for (AfterSaleItemTypeEnum typeEnun : values()) {
            allowableValues.add(typeEnun.getCode());
        }
        return allowableValues;
    }
}