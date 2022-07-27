package moe.ahao.commerce.order.infrastructure.gateway;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import moe.ahao.commerce.market.api.command.LockUserCouponCommand;
import moe.ahao.commerce.market.api.dto.UserCouponDTO;
import moe.ahao.commerce.market.api.query.GetUserCouponQuery;
import moe.ahao.commerce.order.infrastructure.gateway.feign.CouponFeignClient;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CouponGateway {
    @Autowired
    private CouponFeignClient couponFeignClient;

    /**
     * 获取用户优惠券
     */
    public UserCouponDTO get(GetUserCouponQuery query) {
        Result<UserCouponDTO> result = couponFeignClient.get(query);
        UserCouponDTO obj = result.getObj();
        return obj;
    }

    /**
     * 锁定用户优惠券
     */
    @SentinelResource(value = "CouponGateway:lock")
    public Boolean lock(LockUserCouponCommand command) {
        Result<Boolean> result = couponFeignClient.lock(command);
        Boolean obj = result.getObj();
        return obj;

    }
}
