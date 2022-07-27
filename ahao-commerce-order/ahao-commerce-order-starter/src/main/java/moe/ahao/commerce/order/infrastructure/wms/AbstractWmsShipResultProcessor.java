package moe.ahao.commerce.order.infrastructure.wms;


import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;
import moe.ahao.commerce.order.infrastructure.component.OrderOperateLogFactory;
import moe.ahao.commerce.order.infrastructure.domain.dto.WmsShipDTO;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderOperateLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractWmsShipResultProcessor implements OrderWmsShipResultProcessor {
    @Autowired
    protected OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderOperateLogMapper orderOperateLogMapper;

    @Autowired
    protected OrderOperateLogFactory orderOperateLogFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void execute(WmsShipDTO wmsShipDTO) throws OrderException {
        // 1. 查询订单
        OrderInfoDO order = orderInfoMapper.selectOneByOrderId(wmsShipDTO.getOrderId());
        if (order == null) {
            return;
        }

        // 2. 校验订单状态
        if (!this.checkOrderStatus(order)) {
            return;
        }

        // 3. 执行具体的业务逻辑
        this.doExecute(wmsShipDTO, order);

        // 4. 更新订单状态
        OrderStatusChangeEnum statusChange = wmsShipDTO.getStatusChange();
        Integer formStatus = statusChange.getPreStatus().getCode();
        Integer toStatus = statusChange.getCurrentStatus().getCode();
        orderInfoMapper.updateOrderStatusByOrderId(order.getOrderId(), formStatus, toStatus);

        // 5. 增加操作日志
        orderOperateLogMapper.insert(orderOperateLogFactory.get(order, wmsShipDTO.getStatusChange()));
    }

    /**
     * 校验订单状态
     */
    protected abstract boolean checkOrderStatus(OrderInfoDO order);

    /**
     * 执行具体的业务逻辑
     */
    protected abstract void doExecute(WmsShipDTO wmsShipDTO, OrderInfoDO order);
}
