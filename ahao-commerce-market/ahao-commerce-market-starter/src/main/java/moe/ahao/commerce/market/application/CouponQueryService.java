package moe.ahao.commerce.market.application;

import moe.ahao.commerce.market.api.dto.UserCouponDTO;
import moe.ahao.commerce.market.api.query.GetUserCouponQuery;
import moe.ahao.commerce.market.infrastructure.exception.MarketExceptionEnum;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data.CouponConfigDO;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data.CouponDO;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.mapper.CouponConfigMapper;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.mapper.CouponMapper;
import moe.ahao.exception.CommonBizExceptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CouponQueryService {

    @Autowired
    private CouponConfigMapper couponConfigMapper;

    @Autowired
    private CouponMapper couponMapper;

    /**
     * 查询用户的优惠券信息
     */
    public UserCouponDTO query(GetUserCouponQuery userCouponQuery) {
        // 入参检查
        String userId = userCouponQuery.getUserId();
        String couponId = userCouponQuery.getCouponId();
        if(StringUtils.isAnyEmpty(userId, couponId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // 判断用户优惠券是否存在
        CouponDO couponDO = couponMapper.selectOneByUserIdAndCouponId(userId, couponId);
        if (couponDO == null) {
            throw MarketExceptionEnum.USER_COUPON_IS_NULL.msg();
        }
        String couponConfigId = couponDO.getCouponConfigId();

        // 判断优惠券活动配置信息是否存在
        CouponConfigDO couponConfigDO = couponConfigMapper.selectOneByCouponConfigId(couponConfigId);
        if (couponConfigDO == null) {
            throw MarketExceptionEnum.USER_COUPON_CONFIG_IS_NULL.msg();
        }

        // 返回数据
        UserCouponDTO userCouponDTO = new UserCouponDTO();
        userCouponDTO.setUserId(userId);
        userCouponDTO.setCouponConfigId(couponConfigId);
        userCouponDTO.setCouponId(couponId);
        userCouponDTO.setName(couponConfigDO.getName());
        userCouponDTO.setType(couponConfigDO.getType());
        userCouponDTO.setAmount(couponConfigDO.getAmount());
        userCouponDTO.setConditionAmount(couponConfigDO.getConditionAmount());
        userCouponDTO.setValidStartTime(couponConfigDO.getValidStartTime());
        userCouponDTO.setValidEndTime(couponConfigDO.getValidEndTime());
        userCouponDTO.setUsed(couponDO.getUsed());
        return userCouponDTO;
    }
}
