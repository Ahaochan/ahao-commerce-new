package moe.ahao.commerce.market.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券类型
 */
@Getter
@AllArgsConstructor
public enum CouponTypeEnum {
    CASH_COUPON(1, "现金券"),
    REACH_DISCOUNT_COUPON(2, "满减券"),
    ;
    private final Integer code;
    private final String msg;
}
