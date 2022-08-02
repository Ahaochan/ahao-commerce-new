package com.ruyuan.eshop.order.elasticsearch.annotation;

import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;

import java.lang.annotation.*;


/**
 * es 文档注解
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EsDocument {
    /**
     * index 索引名称
     */
    EsIndexNameEnum index();
}
