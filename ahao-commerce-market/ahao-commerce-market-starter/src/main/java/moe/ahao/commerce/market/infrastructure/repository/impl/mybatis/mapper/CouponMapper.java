package moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data.CouponDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 用户优惠券记录表 Mapper 接口
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {
    /**
     * 查询优惠券
     */
    CouponDO selectOneByUserIdAndCouponId(@Param("userId") String userId, @Param("couponId") String couponId);

    /**
     * 更新优惠券的使用状态
     */
    int updateUsedById(@Param("used") Integer used, @Param("usedTime") Date usedTime, @Param("id") Long id);
}
