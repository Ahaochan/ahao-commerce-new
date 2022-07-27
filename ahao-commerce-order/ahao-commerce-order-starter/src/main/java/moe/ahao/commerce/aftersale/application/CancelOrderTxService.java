package moe.ahao.commerce.aftersale.application;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.CancelOrderCommand;
import moe.ahao.commerce.aftersale.infrastructure.enums.OrderCancelTypeEnum;
import moe.ahao.commerce.common.enums.OrderOperateTypeEnum;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.fulfill.api.command.CancelFulfillCommand;
import moe.ahao.commerce.order.infrastructure.gateway.FulfillGateway;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderOperateLogDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderOperateLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
/* package */ class CancelOrderTxService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderOperateLogMapper orderOperateLogMapper;

    @Autowired
    private FulfillGateway fulfillGateway;

    @GlobalTransactional(rollbackFor = Exception.class)
    public void cancelFulfillmentAndUpdateOrderStatus(CancelOrderCommand command) {
        String orderId = command.getOrderId();
        Integer cancelType = command.getCancelType();
        OrderInfoDO orderInfo = orderInfoMapper.selectOneByOrderId(orderId);

        // 1. 履约取消
        this.cancelFulfill(orderInfo);
        // 2. 更新订单状态和记录订单操作日志
        this.updateOrderStatusAndSaveOperationLog(orderInfo, cancelType);
    }

    /**
     * 调用履约拦截订单
     */
    private void cancelFulfill(OrderInfoDO orderInfo) {
        if (OrderStatusEnum.CREATED.getCode().equals(orderInfo.getOrderStatus())) {
            log.info("取消订单拦截履约, 当前订单{}状态为:{}, 无需拦截履约", orderInfo.getOrderId(), orderInfo.getOrderStatus());
            return;
        }
        CancelFulfillCommand command = new CancelFulfillCommand();
        command.setBusinessIdentifier(orderInfo.getBusinessIdentifier());
        command.setOrderId(orderInfo.getOrderId());
        command.setParentOrderId(orderInfo.getParentOrderId());
        command.setBusinessOrderId(orderInfo.getBusinessOrderId());
        command.setOrderType(orderInfo.getOrderType());
        command.setOrderStatus(orderInfo.getOrderStatus());
        command.setCancelType(orderInfo.getCancelType());
        command.setCancelTime(orderInfo.getCancelTime());
        command.setSellerId(orderInfo.getSellerId());
        command.setUserId(orderInfo.getUserId());
        command.setPayType(orderInfo.getPayType());
        command.setTotalAmount(orderInfo.getTotalAmount());
        command.setPayAmount(orderInfo.getPayAmount());
        command.setCouponId(orderInfo.getCouponId());
        command.setPayTime(orderInfo.getPayTime());
        // TODO command.setCancelDeadlineTime(orderInfo.getCancelTime());
        command.setSellerRemark(orderInfo.getUserRemark());
        command.setUserRemark(orderInfo.getUserRemark());
        command.setUserType(orderInfo.getOrderType());
        command.setDeleteStatus(orderInfo.getDeleteStatus());
        command.setCommentStatus(orderInfo.getCommentStatus());

        fulfillGateway.cancelFulfill(command);
    }

    /**
     * 更新订单状态和记录订单操作日志
     */
    private void updateOrderStatusAndSaveOperationLog(OrderInfoDO orderInfo, Integer cancelType) {
        String orderId = orderInfo.getOrderId();
        OrderCancelTypeEnum orderCancelTypeEnum = OrderCancelTypeEnum.getByCode(cancelType);

        // 1. 更新订单表
        orderInfoMapper.updateCancelInfoByOrderId(orderId, cancelType, OrderStatusEnum.CANCELED.getCode(), new Date());
        log.info("更新订单信息OrderInfo状态: orderId:{},status:{}", orderInfo.getOrderId(), orderInfo.getOrderStatus());

        // 2. 新增订单操作操作日志表
        OrderOperateLogDO orderOperateLogDO = new OrderOperateLogDO();
        orderOperateLogDO.setOrderId(orderId);
        orderOperateLogDO.setPreStatus(orderInfo.getOrderStatus());
        orderOperateLogDO.setCurrentStatus(OrderStatusEnum.CANCELED.getCode());
        if (OrderCancelTypeEnum.USER_CANCELED == orderCancelTypeEnum) {
            orderOperateLogDO.setOperateType(OrderOperateTypeEnum.MANUAL_CANCEL_ORDER.getCode());
            orderOperateLogDO.setRemark(OrderOperateTypeEnum.MANUAL_CANCEL_ORDER.getName() + orderOperateLogDO.getPreStatus() + "-" + orderOperateLogDO.getCurrentStatus());
        } else if (OrderCancelTypeEnum.TIMEOUT_CANCELED == orderCancelTypeEnum) {
            orderOperateLogDO.setOperateType(OrderOperateTypeEnum.AUTO_CANCEL_ORDER.getCode());
            orderOperateLogDO.setRemark(OrderOperateTypeEnum.AUTO_CANCEL_ORDER.getName() + orderOperateLogDO.getPreStatus() + "-" + orderOperateLogDO.getCurrentStatus());
        } else {
            orderOperateLogDO.setOperateType(OrderOperateTypeEnum.AUTO_CANCEL_ORDER.getCode());
        }
        orderOperateLogMapper.insert(orderOperateLogDO);
        log.info("新增订单操作日志OrderOperateLog状态, orderId:{}, PreStatus:{}, CurrentStatus:{}", orderInfo.getOrderId(),
            orderOperateLogDO.getPreStatus(), orderOperateLogDO.getCurrentStatus());
    }
}
