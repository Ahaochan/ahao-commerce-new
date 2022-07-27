package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderAutoNoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单编号表 Mapper 接口
 */
@Mapper
public interface OrderAutoNoMapper extends BaseMapper<OrderAutoNoDO> {

}
