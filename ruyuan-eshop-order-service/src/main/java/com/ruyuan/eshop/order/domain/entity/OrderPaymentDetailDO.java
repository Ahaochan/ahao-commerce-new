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
 * 订单支付明细表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("order_payment_detail")
public class OrderPaymentDetailDO implements Serializable {

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
     * 账户类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer accountType;

    /**
     * 支付类型  10:微信支付, 20:支付宝支付
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer payType;

    /**
     * 支付状态 10:未支付,20:已支付
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer payStatus;

    /**
     * 支付金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer payAmount;

    /**
     * 支付时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date payTime;

    /**
     * 支付流水号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String outTradeNo;

    /**
     * 支付备注信息
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String payRemark;
}
