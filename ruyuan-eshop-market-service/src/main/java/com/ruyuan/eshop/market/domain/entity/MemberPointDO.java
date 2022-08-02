package com.ruyuan.eshop.market.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 会员积分表
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@TableName("member_point")
public class MemberPointDO {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 用户账号id
     */
    private String userId;
    /**
     * 会员积分
     */
    private Integer point;
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
