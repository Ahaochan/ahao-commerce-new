package moe.ahao.commerce.market.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券发放类型
 */
@Getter
@AllArgsConstructor
public enum CouponGiveOutTypeEnum {
    ACHIEVABLE_AND_GIVE_OUT(1, "可领取可发放"),
    ONLY_GIVE_OUT(1, "仅可发放"),
    ONLY_ACHIEVABLE(2, "仅可领取"),
    ;
    private final Integer code;
    private final String msg;
}
