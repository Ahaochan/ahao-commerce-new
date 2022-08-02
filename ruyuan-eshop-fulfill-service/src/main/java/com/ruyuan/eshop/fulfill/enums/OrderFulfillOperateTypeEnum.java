package com.ruyuan.eshop.fulfill.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.ruyuan.eshop.fulfill.enums.OrderFulfillStatusEnum.*;

/**
 * 履约单操作类型枚举值
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum OrderFulfillOperateTypeEnum {

    NEW_ORDER(10, "新建履约单", NULL, FULFILL),

    OUT_STOCK_ORDER(20, "履约单出库", FULFILL, OUT_STOCK),

    DELIVER_ORDER(30, "配送履约单", OUT_STOCK, DELIVERY),

    SIGN_ORDER(40, "签收履约单", DELIVERY, SIGNED),

    CANCEL_ORDER(50, "取消履约单", FULFILL, CANCELLED),
    ;

    private Integer code;
    private String msg;
    private OrderFulfillStatusEnum fromStatus;
    private OrderFulfillStatusEnum toStatus;


    public static Map<Integer, String> toMap() {
        Map<Integer, String> map = new HashMap<>(16);
        for (OrderFulfillOperateTypeEnum element : OrderFulfillOperateTypeEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static OrderFulfillOperateTypeEnum getByCode(Integer code) {
        for (OrderFulfillOperateTypeEnum element : OrderFulfillOperateTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}