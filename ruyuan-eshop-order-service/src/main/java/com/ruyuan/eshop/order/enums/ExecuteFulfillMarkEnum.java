package com.ruyuan.eshop.order.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 取消订单时调用履约接口标记枚举类
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public enum ExecuteFulfillMarkEnum {
    CANNOT_EXECUTE_FULFILL(10, "不调用履约"),
    EXECUTE_FULFILL(20, "调用履约");

    private Integer code;

    private String msg;

    ExecuteFulfillMarkEnum(Integer code, String msg) {
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
        for (DeliveryTypeEnum element : DeliveryTypeEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static DeliveryTypeEnum getByCode(Integer code) {
        for (DeliveryTypeEnum element : DeliveryTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}
