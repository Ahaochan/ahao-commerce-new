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
 * 订单售后表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("after_sale_info")
public class AfterSaleInfoDO implements Serializable {

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
     * 售后id
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String afterSaleId;

    /**
     * 接入方业务标识
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer businessIdentifier;

    /**
     * 订单号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderId;

    /**
     * 订单来源渠道
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer orderSourceChannel;

    /**
     * 购买用户id
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String userId;

    /**
     * 订单类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer orderType;

    /**
     * 申请售后来源
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer applySource;

    /**
     * 申请售后时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date applyTime;

    /**
     * 申请原因编码
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer applyReasonCode;

    /**
     * 申请原因
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String applyReason;

    /**
     * 审核时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date reviewTime;

    /**
     * 客服审核来源
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer reviewSource;

    /**
     * 客服审核结果编码
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer reviewReasonCode;

    /**
     * 客服审核结果
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String reviewReason;

    /**
     * 售后类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer afterSaleType;

    /**
     * 售后类型详情枚举
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer afterSaleTypeDetail;

    /**
     * 售后单状态
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer afterSaleStatus;

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
     * 备注
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String remark;


}
