package com.ruyuan.eshop.order.elasticsearch.enums;

import lombok.Getter;

/**
 * es数据类型枚举
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Getter
public enum EsDataTypeEnum {
    /**
     * text
     */
    TEXT("text"),

    KEYWORD("keyword"),

    INTEGER("integer"),

    LONG("long"),

    DOUBLE("double"),

    DATE("date"),

    /**
     * 单条数据
     */
    OBJECT("object"),

    ARRAY("array"),

    BOOLEAN("boolean"),

    /**
     * 嵌套数组
     */
    NESTED("nested"),

    ;


    EsDataTypeEnum(String type) {
        this.type = type;
    }

    private final String type;


}
