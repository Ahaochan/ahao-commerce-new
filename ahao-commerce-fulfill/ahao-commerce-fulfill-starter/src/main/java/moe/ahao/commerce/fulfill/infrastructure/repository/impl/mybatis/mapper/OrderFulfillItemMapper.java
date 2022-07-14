package moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.data.OrderFulfillItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单履约条目 Mapper 接口
 */
@Mapper
public interface OrderFulfillItemMapper extends BaseMapper<OrderFulfillItemDO> {
    int deleteByFulfillId(String fulfillId);

    List<OrderFulfillItemDO> selectListByFulfillId(@Param("fulfillId") String fulfillId);
}
