package moe.ahao.commerce.market.api;

import moe.ahao.commerce.market.api.command.LockUserCouponCommand;
import moe.ahao.commerce.market.api.command.ReleaseUserCouponCommand;
import moe.ahao.commerce.market.api.dto.UserCouponDTO;
import moe.ahao.commerce.market.api.query.GetUserCouponQuery;
import moe.ahao.domain.entity.Result;

public interface CouponApi {
    /**
     * 查询用户的优惠券
     */
    Result<UserCouponDTO> get(GetUserCouponQuery query);

    /**
     * 锁定用户优惠券记录
     */
    Result<Boolean> lock(LockUserCouponCommand command);

    /**
     * 释放用户已使用的优惠券
     */
    Result<Boolean> release(ReleaseUserCouponCommand command);
}
