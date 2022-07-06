package moe.ahao.commerce.market.api.query;

import lombok.Data;

@Data
public class GetUserCouponQuery {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 优惠券ID
     */
    private String couponId;
}
