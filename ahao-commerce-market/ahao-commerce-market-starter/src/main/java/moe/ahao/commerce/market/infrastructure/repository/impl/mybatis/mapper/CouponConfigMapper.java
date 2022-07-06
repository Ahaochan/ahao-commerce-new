package moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data.CouponConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * 优惠券配置表 Mapper 接口
 */
@Mapper
public interface CouponConfigMapper extends BaseMapper<CouponConfigDO> {
    /**
     * 优惠券配置信息
     */
    CouponConfigDO selectOneByCouponConfigId(@Param("couponConfigId") String couponConfigId);
}
