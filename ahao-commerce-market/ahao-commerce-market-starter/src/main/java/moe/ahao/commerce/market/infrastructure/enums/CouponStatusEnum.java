package moe.ahao.commerce.market.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券状态
 */
@Getter
@AllArgsConstructor
public enum CouponStatusEnum {
    UN_STARTED(1, "未开始"),
    GIVING_OUT(2, "发放中"),
    GIVEN_OUT(3, "已发完"),
    EXPIRED(4, "已过期"),
    ;
    private final Integer code;
    private final String msg;

}
