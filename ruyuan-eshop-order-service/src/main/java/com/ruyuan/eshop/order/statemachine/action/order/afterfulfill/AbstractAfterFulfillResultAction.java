package com.ruyuan.eshop.order.statemachine.action.order.afterfulfill;


import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import com.ruyuan.eshop.order.dao.OrderInfoDAO;
import com.ruyuan.eshop.order.dao.OrderOperateLogDAO;
import com.ruyuan.eshop.order.domain.dto.AfterFulfillDTO;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.service.impl.OrderOperateLogFactory;
import com.ruyuan.eshop.order.statemachine.action.OrderStateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

public abstract class AbstractAfterFulfillResultAction extends OrderStateAction<AfterFulfillDTO> {

    @Autowired
    protected OrderInfoDAO orderInfoDAO;

    @Autowired
    protected OrderOperateLogFactory orderOperateLogFactory;

    @Autowired
    private OrderOperateLogDAO orderOperateLogDAO;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    protected OrderInfoDTO onStateChangeInternal(OrderStatusChangeEnum event, AfterFulfillDTO context) {
        //  @Transactional无法生效，需要用编程式事务
        return transactionTemplate.execute(transactionStatus -> {
            // 1、查询订单
            OrderInfoDO order = orderInfoDAO.getByOrderId(context.getOrderId());
            if (null == order) {
                return null;
            }

            // 2、校验订单状态
            OrderStatusEnum orderStatus = OrderStatusEnum.getByCode(order.getOrderStatus());
            if (!handleStatus().equals(orderStatus)) {
                return null;
            }

            // 3、执行具体的业务逻辑
            doExecute(context, order);

            // 4、更新订单状态
            changeOrderStatus(order, context);

            // 5、增加操作日志
            saveOrderOperateLog(order, context);

            // 6、返回标准订单信息
            return orderConverter.orderInfoDO2DTO(order);
        });
    }

    /**
     * @return 自己需要处理的订单桩体
     */
    protected abstract OrderStatusEnum handleStatus();

    /**
     * 执行具体的业务逻辑
     */
    protected void doExecute(AfterFulfillDTO afterFulfillDTO, OrderInfoDO order) {
        // 默认空实现
    }

    /**
     * 更新订单状态
     */
    private void changeOrderStatus(OrderInfoDO order, AfterFulfillDTO afterFulfillDTO) {
        OrderStatusChangeEnum statusChange = afterFulfillDTO.getStatusChange();
        orderInfoDAO.updateOrderStatus(order.getOrderId(), statusChange.getFromStatus().getCode()
                , statusChange.getToStatus().getCode());
    }

    /**
     * 增加订单操作日志
     */
    private void saveOrderOperateLog(OrderInfoDO order, AfterFulfillDTO afterFulfillDTO) {
        orderOperateLogDAO.save(orderOperateLogFactory.get(order, afterFulfillDTO.getStatusChange()));
    }

}
