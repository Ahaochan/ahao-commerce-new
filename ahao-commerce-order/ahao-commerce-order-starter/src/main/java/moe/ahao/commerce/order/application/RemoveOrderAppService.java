package moe.ahao.commerce.order.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.enums.DeleteStatusEnum;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.order.api.command.RemoveOrderCommand;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class RemoveOrderAppService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    public boolean removeOrders(RemoveOrderCommand command) {
        // 1. 参数校验
        this.check(command);

        // 2. 根据id查询订单
        List<String> orderIds = new ArrayList<>(command.getOrderIds());
        List<OrderInfoDO> orders = orderInfoMapper.selectListByOrderIds(orderIds);
        if (CollectionUtils.isEmpty(orders)) {
            return true;
        }

        // 3. 校验订单是否可以移除
        List<Long> ids = new ArrayList<>();
        for (OrderInfoDO order : orders) {
            if (!this.canRemove(order)) {
                throw OrderExceptionEnum.ORDER_CANNOT_REMOVE.msg();
            }
            ids.add(order.getId());
        }

        // 4. 对订单进行软删除
        orderInfoMapper.updateDeleteStatusByIds(ids, DeleteStatusEnum.YES.getCode());
        return true;
    }

    private void check(RemoveOrderCommand command) {
        Set<String> orderIdSet = command.getOrderIds();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(orderIdSet)) {
            throw OrderExceptionEnum.ORDER_ID_IS_NULL.msg();
        }
        int count = orderIdSet.size();
        int maxCount = 200;
        if (count > maxCount) {
            throw OrderExceptionEnum.COLLECTION_PARAM_CANNOT_BEYOND_MAX_SIZE.msg(count, maxCount);
        }
    }

    private boolean canRemove(OrderInfoDO order) {
        return OrderStatusEnum.canRemoveStatus().contains(order.getOrderStatus()) &&
            DeleteStatusEnum.NO.getCode().equals(order.getDeleteStatus());
    }
}
