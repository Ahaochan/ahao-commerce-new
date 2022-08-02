package com.ruyuan.eshop.market.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;


/**
 * 会员积分明细
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@TableName("member_point_detail")
public class MemberPointDetailDO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会员积分ID
     */
    private Long memberPointId;
    /**
     * 用户账号id
     */
    private String userId;
    /**
     * 本次变更之前的积分
     */
    private Integer oldPoint;
    /**
     * 本次变更的积分
     */
    private Integer updatedPoint;
    /**
     * 本次变更之后的积分
     */
    private Integer newPoint;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;
    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
