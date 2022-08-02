package com.ruyuan.eshop.order.domain.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsField;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 订单价格表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("order_amount")
public class OrderAmountDO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date gmtCreate;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date gmtModified;

    /**
     * 订单编号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderId;

    /**
     * 收费类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer amountType;

    /**
     * 收费金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer amount;
}
