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
 * 售后退款单表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("after_sale_refund")
public class AfterSaleRefundDO implements Serializable {

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
     * 售后单号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String afterSaleId;

    /**
     * 订单号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderId;

    /**
     * 售后批次号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String afterSaleBatchNo;

    /**
     * 账户类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer accountType;

    /**
     * 支付类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer payType;

    /**
     * 退款状态
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer refundStatus;

    /**
     * 退款金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer refundAmount;

    /**
     * 退款支付时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date refundPayTime;

    /**
     * 交易单号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String outTradeNo;

    /**
     * 备注
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String remark;

}
