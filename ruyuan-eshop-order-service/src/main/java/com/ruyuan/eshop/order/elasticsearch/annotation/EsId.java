package com.ruyuan.eshop.order.elasticsearch.annotation;

import java.lang.annotation.*;

/**
 * 标识字段作为es文档的id
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface EsId {

}
