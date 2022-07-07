package moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data.DeliveryOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 出库单 Mapper 接口
 */
@Mapper
public interface DeliveryOrderMapper extends BaseMapper<DeliveryOrderDO> {
    List<DeliveryOrderDO> selectListByOrderId(@Param("orderId") String orderId);

    int deleteListByOrderId(@Param("orderId") String orderId);
}
