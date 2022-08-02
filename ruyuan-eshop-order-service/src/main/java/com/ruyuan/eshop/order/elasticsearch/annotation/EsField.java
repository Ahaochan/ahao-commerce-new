package com.ruyuan.eshop.order.elasticsearch.annotation;

import com.ruyuan.eshop.order.elasticsearch.enums.EsAnalyzerEnum;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;

import java.lang.annotation.*;

/**
 * es field
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface EsField {
    /**
     * 数据类型
     *
     * @return
     */
    EsDataTypeEnum type() default EsDataTypeEnum.TEXT;

    /**
     * 指定分词器
     *
     * @return
     */
    EsAnalyzerEnum analyzer() default EsAnalyzerEnum.STANDARD;
}
