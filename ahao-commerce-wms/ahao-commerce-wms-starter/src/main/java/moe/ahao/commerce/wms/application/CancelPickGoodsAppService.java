package moe.ahao.commerce.wms.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.wms.api.command.CancelPickGoodsCommand;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data.DeliveryOrderDO;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.mapper.DeliveryOrderItemMapper;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.mapper.DeliveryOrderMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CancelPickGoodsAppService {
    @Autowired
    private DeliveryOrderMapper deliveryOrderMapper;
    @Autowired
    private DeliveryOrderItemMapper deliveryOrderItemMapper;

    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelPickGoods(CancelPickGoodsCommand command) {
        String orderId = command.getOrderId();

        // 1. 查询出库单
        List<DeliveryOrderDO> deliveryOrders = deliveryOrderMapper.selectListByOrderId(orderId);
        if (CollectionUtils.isEmpty(deliveryOrders)) {
            return true;
        }
        List<Long> ids = new ArrayList<>();
        List<String> deliveryOrderIds = new ArrayList<>();
        for (DeliveryOrderDO deliveryOrder : deliveryOrders) {
            ids.add(deliveryOrder.getId());
            deliveryOrderIds.add(deliveryOrder.getDeliveryOrderId());
        }

        // 2. 移除出库单
        deliveryOrderMapper.deleteBatchIds(ids);

        // 3. 移除条目
        deliveryOrderItemMapper.deleteListByDeliveryOrderIds(deliveryOrderIds);
        return true;
    }
}
