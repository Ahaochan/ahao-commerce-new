package com.ruyuan.eshop.fulfill.enums;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 履约单状态
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public enum OrderFulfillStatusEnum {

    NULL(0, "未知"),
    FULFILL(20, "已履约"),
    OUT_STOCK(30, "出库"),
    DELIVERY(40, "配送中"),
    SIGNED(50, "已签收"),
    CANCELLED(100, "已取消"),
    ;


    private Integer code;

    private String msg;

    OrderFulfillStatusEnum(Integer code, String msg) {
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
        for (OrderFulfillStatusEnum element : OrderFulfillStatusEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static OrderFulfillStatusEnum getByCode(Integer code) {
        for (OrderFulfillStatusEnum element : OrderFulfillStatusEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

    public static Set<Integer> allowableValues() {
        Set<Integer> allowableValues = new HashSet<>(values().length);
        for (OrderFulfillStatusEnum orderStatusEnum : values()) {
            allowableValues.add(orderStatusEnum.getCode());
        }
        return allowableValues;
    }

    /**
     * 不能取消履约的状态
     *
     * @return
     */
    public static Set<Integer> notCancelStatus() {
        Set<Integer> set = new HashSet<>(values().length);
        set.add(OUT_STOCK.code);
        set.add(DELIVERY.code);
        set.add(SIGNED.code);
        set.add(CANCELLED.code);
        return set;
    }
}