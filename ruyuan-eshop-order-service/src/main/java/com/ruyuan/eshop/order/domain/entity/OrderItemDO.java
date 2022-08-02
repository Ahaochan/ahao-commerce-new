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
 * 订单条目表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("order_item")
public class OrderItemDO implements Serializable {

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
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderId;

    /**
     * 订单明细编号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderItemId;

    /**
     * 商品类型 1:普通商品,2:预售商品
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer productType;

    /**
     * 商品编号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String productId;

    /**
     * 商品图片
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String productImg;

    /**
     * 商品名称
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String productName;

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
     * 当前商品支付原总价
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer originAmount;

    /**
     * 交易支付金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer payAmount;

    /**
     * 商品单位
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String productUnit;

    /**
     * 采购成本价
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer purchasePrice;

    /**
     * 卖家ID
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String sellerId;

    /**
     * 扩展信息
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String extJson;
}
