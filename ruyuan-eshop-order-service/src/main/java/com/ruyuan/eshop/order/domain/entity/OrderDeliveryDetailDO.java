package com.ruyuan.eshop.order.domain.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsField;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 订单配送信息表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("order_delivery_detail")
public class OrderDeliveryDetailDO implements Serializable {

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
     * 配送类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer deliveryType;

    /**
     * 省
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String province;

    /**
     * 市
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String city;

    /**
     * 区
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String area;

    /**
     * 街道
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String street;

    /**
     * 详细地址
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String detailAddress;

    /**
     * 经度
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private BigDecimal lon;

    /**
     * 维度
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private BigDecimal lat;

    /**
     * 收货人姓名
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String receiverName;

    /**
     * 收货人电话
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String receiverPhone;

    /**
     * 调整地址次数
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer modifyAddressCount;

    /**
     * 配送员编号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String delivererNo;

    /**
     * 配送员姓名
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String delivererName;

    /**
     * 配送员手机号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String delivererPhone;

    /**
     * 出库时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date outStockTime;

    /**
     * 签收时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date signedTime;
}
