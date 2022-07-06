package moe.ahao.commerce.market.api;

import moe.ahao.commerce.market.api.command.LockUserCouponCommand;
import moe.ahao.commerce.market.api.command.ReleaseUserCouponCommand;
import moe.ahao.commerce.market.api.dto.UserCouponDTO;
import moe.ahao.commerce.market.api.query.GetUserCouponQuery;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface CouponFeignApi extends CouponApi {
    String CONTEXT = "/api/coupon";
    @PostMapping("/get")
    Result<UserCouponDTO> get(@RequestBody GetUserCouponQuery query);
    @PostMapping("/lock")
    Result<Boolean> lock(@RequestBody LockUserCouponCommand command);
    @PostMapping("/release")
    Result<Boolean> release(@RequestBody ReleaseUserCouponCommand command);
}
