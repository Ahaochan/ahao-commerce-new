package com.ruyuan.eshop.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 售后单操作类型枚举值
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public enum AfterSaleOperateTypeEnum {

    NEW_AFTER_SALE(10, "新建售后单"),

    NEW_LACK_AFTER_SALE(20, "新建缺品售后单"),

    REVIEW_AFTER_SALE_PASS(30, "售后单审核通过"),

    REVIEW_AFTER_SALE_REJECTION(40, "售后单审核拒绝"),

    REVOKE_AFTER_SALE(50, "撤销售后单"),

    AFTER_SALE_REFUNDING(60, "售后单退款中"),

    AFTER_SALE_REFUNDED(70, "售后单退款成功"),

    AFTER_SALE_REFUND_FAIL(80, "售后单退款失败"),

    ;

    private Integer code;

    private String msg;

    AfterSaleOperateTypeEnum(Integer code, String msg) {
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
        for (AfterSaleOperateTypeEnum element : AfterSaleOperateTypeEnum.values()) {
            map.put(element.getCode(), element.getMsg());
        }
        return map;
    }

    public static AfterSaleOperateTypeEnum getByCode(Integer code) {
        for (AfterSaleOperateTypeEnum element : AfterSaleOperateTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }
}