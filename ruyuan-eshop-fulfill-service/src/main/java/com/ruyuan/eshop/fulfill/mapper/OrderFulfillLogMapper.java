package com.ruyuan.eshop.fulfill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单履约操作日志表 Mapper 接口
 * </p>
 *
 * @author zhonghuashishan
 */
@Mapper
public interface OrderFulfillLogMapper extends BaseMapper<OrderFulfillLogDO> {

}
