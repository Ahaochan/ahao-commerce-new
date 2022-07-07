package moe.ahao.commerce.wms.infrastructure.gateway.impl.demo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data.DeliveryOrderDO;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data.DeliveryOrderItemDO;

import java.util.List;

/**
 * 调度出库结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDeliveryDTO {
    /**
     * 调度出库单
     */
    private DeliveryOrderDO deliveryOrder;
    /**
     * 调度出库单条目
     */
    private List<DeliveryOrderItemDO> deliveryOrderItems;
}
