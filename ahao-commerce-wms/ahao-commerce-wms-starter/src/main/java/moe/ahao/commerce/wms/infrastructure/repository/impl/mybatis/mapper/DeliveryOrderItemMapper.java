package moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data.DeliveryOrderItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 出库单条目 Mapper 接口
 */
@Mapper
public interface DeliveryOrderItemMapper extends BaseMapper<DeliveryOrderItemDO> {
    List<DeliveryOrderItemDO> selectListByDeliveryOrderId(@Param("deliveryOrderId") String deliveryOrderId);
    List<DeliveryOrderItemDO> selectListByDeliveryOrderIds(@Param("deliveryOrderIds") List<String> deliveryOrderIds);

    int deleteListByDeliveryOrderIds(@Param("deliveryOrderIds") List<String> deliveryOrderIds);
}
