package com.ruyuan.eshop.common.enums;

import com.google.common.collect.Lists;

import java.util.*;

/**
 * 订单号状态枚举
 * @author zhonghuashishan
 * @version 1.0
 */
public enum OrderStatusEnum {

    NULL(0, "未知"),
    CREATED(10, "已创建"),
    PAID(20, "已支付"),
    FULFILL(30, "已履约"),
    OUT_STOCK(40, "出库"),
    DELIVERY(50, "配送中"),
    SIGNED(60, "已签收"),
    CANCELED(70, "已取消"),
    REFUSED(100, "已拒收"),
    INVALID(127, "无效订单");


    private Integer code;

    private String msg;

    OrderStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static Map<Integer,String> toMap() {
        Map<Integer,String> map = new HashMap<>(16);
        for (OrderStatusEnum element : OrderStatusEnum.values() ){
            map.put(element.getCode(),element.getMsg());
        }
        return map;
    }

    public static OrderStatusEnum getByCode(Integer code) {
        for(OrderStatusEnum element : OrderStatusEnum.values()){
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

    /**
     * 未出库订单状态
     * @return
     */
    public static List<Integer> unOutStockStatus() {
        return Lists.newArrayList(
                CREATED.code,
                PAID.code,
                FULFILL.code
        );
    }

    /**
     * 可以移除的订单状态
     * @return
     */
    public static List<Integer> canRemoveStatus() {
        return Lists.newArrayList(
                SIGNED.code,
                CANCELED.code,
                REFUSED.code,
                INVALID.code
        );
    }

    /**
     * 未支付的订单状态
     * @return
     */
    public static List<Integer> unPaidStatus() {
        return Lists.newArrayList(
                CREATED.code
        );
    }

    public static Set<Integer> allowableValues() {
        Set<Integer> allowableValues = new HashSet<>(values().length);
        for (OrderStatusEnum orderStatusEnum : values()) {
            allowableValues.add(orderStatusEnum.getCode());
        }
        return allowableValues;
    }

    /**
     * 有效订单状态
     * @return
     */
    public static Set<Integer> validStatus() {
        Set<Integer> validStatus = allowableValues();
        validStatus.remove(INVALID.code);
        return validStatus;
    }

    /**
     * 可以发起缺品的状态(支付之后，配送之前)
     * @return
     */
    public static Set<Integer> canLack() {
        Set<Integer> validStatus = allowableValues();
        validStatus.remove(FULFILL.code);
        validStatus.remove(OUT_STOCK.code);
        return validStatus;
    }
}