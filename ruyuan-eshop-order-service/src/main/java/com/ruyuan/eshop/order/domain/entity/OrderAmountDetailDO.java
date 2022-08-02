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
 * 订单价格明细表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("order_amount_detail")
public class OrderAmountDetailDO implements Serializable {

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
     * 产品类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer productType;

    /**
     * 订单明细编号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderItemId;

    /**
     * 商品编号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String productId;

    /**
     * sku编码
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String skuCode;

    /**
     * 销售数量
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer saleQuantity;

    /**
     * 销售单价
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer salePrice;

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
