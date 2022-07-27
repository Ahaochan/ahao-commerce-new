package moe.ahao.commerce.order.infrastructure.component;

import moe.ahao.commerce.common.enums.OrderOperateTypeEnum;
import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderOperateLogDO;
import org.springframework.stereotype.Component;

/**
 * 订单操作日志工厂
 */
@Component
public class OrderOperateLogFactory {

    /**
     * 获取订单操作日志
     *
     * @param statusChange 订单状态变更
     * @return 订单操作内容
     */
    public OrderOperateLogDO get(OrderInfoDO order, OrderStatusChangeEnum statusChange) {
        OrderOperateTypeEnum operateType = statusChange.getOperateType();
        Integer preStatus = statusChange.getPreStatus().getCode();
        Integer currentStatus = statusChange.getCurrentStatus().getCode();
        return create(order, operateType, preStatus, currentStatus, operateType.getName());
    }

    /**
     * 创建订单操作日志
     */
    private OrderOperateLogDO create(OrderInfoDO order, OrderOperateTypeEnum operateType, int preStatus, int currentStatus, String operateRemark) {
        OrderOperateLogDO log = new OrderOperateLogDO();

        log.setOrderId(order.getOrderId());
        log.setOperateType(operateType.getCode());
        log.setPreStatus(preStatus);
        log.setCurrentStatus(currentStatus);
        log.setRemark(operateRemark);

        return log;
    }


}
