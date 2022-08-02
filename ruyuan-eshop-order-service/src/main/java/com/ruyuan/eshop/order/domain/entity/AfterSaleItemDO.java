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
 * 订单售后详情表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("after_sale_item")
public class AfterSaleItemDO implements Serializable {
    private static final long serialVersionUID = -908961610293643135L;

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
     * 售后id
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String afterSaleId;

    /**
     * 订单id
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderId;

    /**
     * sku code
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String skuCode;

    /**
     * 商品名
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String productName;

    /**
     * 商品图片地址
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String productImg;

    /**
     * 商品退货数量
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer returnQuantity;

    /**
     * 商品总金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer originAmount;

    /**
     * 申请退款金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer applyRefundAmount;

    /**
     * 实际退款金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer realRefundAmount;

    /**
     * 本条目退货完成标记 10:购买的sku未全部退货 20:购买的sku已全部退货
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer returnCompletionMark;

    /**
     * 售后条目类型 10:售后订单条目 20:尾笔条目退优惠券 30:尾笔条目退运费
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer afterSaleItemType;
}
