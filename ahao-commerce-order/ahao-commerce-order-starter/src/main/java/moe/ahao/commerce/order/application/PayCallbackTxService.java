package moe.ahao.commerce.order.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.enums.OrderOperateTypeEnum;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.order.infrastructure.enums.PayStatusEnum;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderOperateLogDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderOperateLogMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderPaymentDetailMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service.OrderOperateLogMybatisService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PayCallbackTxService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderPaymentDetailMapper orderPaymentDetailMapper;
    @Autowired
    private OrderOperateLogMapper orderOperateLogMapper;
    @Autowired
    private OrderOperateLogMybatisService orderOperateLogMybatisService;

    /**
     * 支付回调更新订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusWhenPayCallback(OrderInfoDO orderInfo) {
        // 1. 更新主单的状态
        this.updateMasterOrderStatus(orderInfo);

        // 2. 判断是否存在子订单
        String orderId = orderInfo.getOrderId();
        List<OrderInfoDO> subOrderInfoList = orderInfoMapper.selectListByParentOrderId(orderId);
        if (CollectionUtils.isEmpty(subOrderInfoList)) {
            return;
        }

        // 3. 更新子单的状态
        this.updateSubOrderStatus(orderInfo, subOrderInfoList);
    }

    /**
     * 更新主订单状态
     */
    private void updateMasterOrderStatus(OrderInfoDO orderInfo) {
        String orderId = orderInfo.getOrderId();
        // 1. 更新主单订单状态
        Integer formStatus = orderInfo.getOrderStatus();
        Integer toStatus = OrderStatusEnum.PAID.getCode();
        List<String> orderIdList = Collections.singletonList(orderId);
        orderInfo.setOrderStatus(toStatus); // 避免后面使用此对象获取的orderStatus不正确
        this.updateOrderStatus(orderIdList, formStatus, toStatus);

        // 2. 更新主单支付状态
        this.updateOrderPayStatus(orderIdList, PayStatusEnum.PAID.getCode());

        // 3. 新增主单订单状态变更日志
        Integer operateType = OrderOperateTypeEnum.PAID_ORDER.getCode();
        String remark = "订单支付回调操作" + formStatus + "-" + toStatus;
        this.insertOrderOperateLog(orderId, operateType, formStatus, toStatus, remark);
    }

    /**
     * 更新子订单状态
     */
    private void updateSubOrderStatus(OrderInfoDO orderInfoDO, List<OrderInfoDO> subOrderInfoDOList) {
        // 1. 先将主订单状态设置为无效订单
        String orderId = orderInfoDO.getOrderId();
        Integer fromStatus = orderInfoDO.getOrderStatus();
        Integer masterToStatus = OrderStatusEnum.INVALID.getCode();
        List<String> orderIdList = Collections.singletonList(orderId);
        this.updateOrderStatus(orderIdList, fromStatus, masterToStatus);

        // 2. 新增订单状态变更日志
        Integer operateType = OrderOperateTypeEnum.PAID_ORDER.getCode();
        String remark = "订单支付回调操作，主订单状态变更" + fromStatus + "-" + masterToStatus;
        this.insertOrderOperateLog(orderId, operateType, fromStatus, masterToStatus, remark);

        // 3. 再更新子订单的状态为已支付
        Integer subToStatus = OrderStatusEnum.PAID.getCode();
        List<String> subOrderIdList = subOrderInfoDOList.stream()
            .map(OrderInfoDO::getOrderId).collect(Collectors.toList());
        this.updateOrderStatus(subOrderIdList, OrderStatusEnum.CREATED.getCode(), subToStatus);

        // 4. 更新子订单的支付明细
        this.updateOrderPayStatus(subOrderIdList, PayStatusEnum.PAID.getCode());

        // 5. 保存子订单操作日志
        this.insertSubOrderOperateLog(subToStatus, subOrderInfoDOList);
    }

    /**
     * 更新订单状态
     */
    private void updateOrderStatus(List<String> orderIdList, Integer formStatus, Integer toStatus) {
        if (orderIdList.size() == 1) {
            orderInfoMapper.updateOrderStatusByOrderId(orderIdList.get(0), formStatus, toStatus);
        } else {
            orderInfoMapper.updateOrderStatusByOrderIds(orderIdList, formStatus, toStatus);
        }
    }

    /**
     * 更新订单支付状态
     */
    private void updateOrderPayStatus(List<String> orderIdList, Integer payStatus) {
        if (orderIdList.size() == 1) {
            orderPaymentDetailMapper.updatePayStatusByOrderId(orderIdList.get(0), payStatus);
        } else {
            orderPaymentDetailMapper.updatePayStatusByOrderIds(orderIdList, payStatus);
        }
    }

    /**
     * 保存订单操作日志
     */
    private void insertOrderOperateLog(String orderId, Integer operateType, Integer preStatus, Integer currentStatus, String remark) {
        OrderOperateLogDO orderOperateLogDO = new OrderOperateLogDO();
        orderOperateLogDO.setOrderId(orderId);
        orderOperateLogDO.setOperateType(operateType);
        orderOperateLogDO.setPreStatus(preStatus);
        orderOperateLogDO.setCurrentStatus(currentStatus);
        orderOperateLogDO.setRemark(remark);

        orderOperateLogMapper.insert(orderOperateLogDO);
    }

    /**
     * 保存子订单操作日志
     */
    private void insertSubOrderOperateLog(Integer subCurrentOrderStatus, List<OrderInfoDO> subOrderInfoList) {
        List<OrderOperateLogDO> subOrderOperateLogList = new ArrayList<>();
        for (OrderInfoDO subOrderInfo : subOrderInfoList) {
            String subOrderId = subOrderInfo.getOrderId();
            Integer subPreOrderStatus = subOrderInfo.getOrderStatus();

            // 订单状态变更日志
            OrderOperateLogDO subOrderOperateLog = new OrderOperateLogDO();
            subOrderOperateLog.setOrderId(subOrderId);
            subOrderOperateLog.setOperateType(OrderOperateTypeEnum.PAID_ORDER.getCode());
            subOrderOperateLog.setPreStatus(subPreOrderStatus);
            subOrderOperateLog.setCurrentStatus(subCurrentOrderStatus);
            subOrderOperateLog.setRemark("订单支付回调操作，子订单状态变更"
                + subOrderOperateLog.getPreStatus() + "-"
                + subOrderOperateLog.getCurrentStatus());
            subOrderOperateLogList.add(subOrderOperateLog);
        }

        // 新增子订单状态变更日志
        if (CollectionUtils.isNotEmpty(subOrderOperateLogList)) {
            orderOperateLogMybatisService.saveBatch(subOrderOperateLogList);
        }
    }

}
