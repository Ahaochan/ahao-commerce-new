package moe.ahao.commerce.market.api.command;

import lombok.Data;

/**
 * 锁定使用优惠券入参
 */
@Data
public class LockUserCouponCommand {
    /**
     * 业务线标识
     */
    private Integer businessIdentifier;
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 卖家ID
     */
    private String sellerId;
    /**
     * 优惠券ID
     */
    private String couponId;
}
