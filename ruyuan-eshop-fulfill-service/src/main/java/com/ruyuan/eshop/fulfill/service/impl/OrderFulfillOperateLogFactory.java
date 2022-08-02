package com.ruyuan.eshop.fulfill.service.impl;

import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillLogDO;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillOperateTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 履约单操作日志工厂
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class OrderFulfillOperateLogFactory {

    /**
     * 获取履约单操作日志
     */
    public OrderFulfillLogDO get(OrderFulfillDO orderFulfill, OrderFulfillOperateTypeEnum operateType) {
        Integer fromStatus = operateType.getFromStatus().getCode();
        Integer toStatus = operateType.getToStatus().getCode();
        return create(orderFulfill, operateType, fromStatus, toStatus, operateType.getMsg());
    }

    /**
     * 创建履约单操作日志
     */
    private OrderFulfillLogDO create(OrderFulfillDO orderFulfill,
                                     OrderFulfillOperateTypeEnum operateType, int preStatus, int currentStatus, String operateRemark) {
        OrderFulfillLogDO log = new OrderFulfillLogDO();
        log.setOrderId(orderFulfill.getOrderId());
        log.setFulfillId(orderFulfill.getFulfillId());
        log.setOperateType(operateType.getCode());
        log.setPreStatus(preStatus);
        log.setCurrentStatus(currentStatus);
        log.setRemark(operateRemark);
        return log;
    }


}
