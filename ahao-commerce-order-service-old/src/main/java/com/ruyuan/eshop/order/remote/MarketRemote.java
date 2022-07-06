package com.ruyuan.eshop.order.remote;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.ruyuan.eshop.order.exception.OrderBizException;
import moe.ahao.commerce.market.api.CouponDubboApi;
import moe.ahao.commerce.market.api.MarketDubboApi;
import moe.ahao.commerce.market.api.command.LockUserCouponCommand;
import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.market.api.dto.UserCouponDTO;
import moe.ahao.commerce.market.api.query.CalculateOrderAmountQuery;
import moe.ahao.commerce.market.api.query.GetUserCouponQuery;
import moe.ahao.domain.entity.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 营销服务远程接口
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class MarketRemote {

    /**
     * 营销服务
     */
    @DubboReference(version = "1.0.0", retries = 0)
    private CouponDubboApi couponApi;
    @DubboReference(version = "1.0.0", retries = 0)
    private MarketDubboApi marketApi;

    /**
     * 计算订单费用
     * @param calculateOrderPriceRequest
     * @return
     */
    @SentinelResource(value = "MarketRemote:calculateOrderAmount")
    public CalculateOrderAmountDTO calculateOrderAmount(CalculateOrderAmountQuery calculateOrderPriceRequest) {
        Result<CalculateOrderAmountDTO> result = marketApi.calculateOrderAmount(calculateOrderPriceRequest);
        // 检查价格计算结果
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderBizException(String.valueOf(result.getCode()), result.getMsg());
        }
        return result.getObj();
    }

    /**
     * 锁定用户优惠券
     * @param lockUserCouponRequest
     * @return
     */
    @SentinelResource(value = "MarketRemote:lockUserCoupon")
    public Boolean lockUserCoupon(LockUserCouponCommand lockUserCouponRequest) {
        Result<Boolean> result = couponApi.lock(lockUserCouponRequest);
        // 检查锁定用户优惠券结果
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderBizException(String.valueOf(result.getCode()), result.getMsg());
        }
        return result.getObj();
    }

    /**
     * 获取用户优惠券
     * @return
     */
    public UserCouponDTO getUserCoupon(GetUserCouponQuery userCouponQuery) {
        Result<UserCouponDTO> result = couponApi.get(userCouponQuery);
        if (result.getCode() == Result.SUCCESS) {
            return result.getObj();
        }
        return null;
    }
}
