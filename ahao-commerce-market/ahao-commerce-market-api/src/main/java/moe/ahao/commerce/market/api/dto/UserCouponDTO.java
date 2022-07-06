package moe.ahao.commerce.market.api.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户优惠券信息
 */
@Data
public class UserCouponDTO {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 优惠券配置ID
     */
    private String couponConfigId;
    /**
     * 优惠券ID
     */
    private String couponId;
    /**
     * 优惠券名称
     */
    private String name;
    /**
     * 优惠券类型，1：现金券，2：满减券
     */
    private Integer type;
    /**
     * 优惠券抵扣金额
     */
    private BigDecimal amount;
    /**
     * 优惠券使用限制金额
     */
    private BigDecimal conditionAmount;
    /**
     * 有效期开始时间
     */
    private Date validStartTime;
    /**
     * 有效期结束时间
     */
    private Date validEndTime;
    /**
     * 是否已使用
     */
    private Integer used;
}
