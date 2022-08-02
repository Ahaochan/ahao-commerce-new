package com.ruyuan.eshop.fulfill.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 履约单操作日志表
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@TableName("order_fulfill_log")
public class OrderFulfillLogDO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 履约单ID
     */
    private String fulfillId;

    /**
     * 操作类型
     */
    private Integer operateType;

    /**
     * 前置状态
     */
    private Integer preStatus;

    /**
     * 当前状态
     */
    private Integer currentStatus;

    /**
     * 备注说明
     */
    private String remark;

}
