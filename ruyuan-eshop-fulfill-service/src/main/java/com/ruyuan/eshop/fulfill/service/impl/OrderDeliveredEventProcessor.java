package com.ruyuan.eshop.fulfill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.message.OrderEvent;
import com.ruyuan.eshop.fulfill.dao.OrderFulfillDAO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillLogDO;
import com.ruyuan.eshop.fulfill.domain.event.OrderDeliveredEvent;
import com.ruyuan.eshop.fulfill.domain.request.TriggerOrderAfterFulfillEventRequest;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillOperateTypeEnum;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单已配送事件处理器
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class OrderDeliveredEventProcessor extends AbstractAfterFulfillEventProcessor {

    @Autowired
    private OrderFulfillDAO orderFulfillDAO;

    @Override
    protected boolean checkFulfillStatus(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill) {
        if (!OrderFulfillStatusEnum.OUT_STOCK.getCode().equals(orderFulfill.getStatus())) {
            log.info("履约单无法配送！orderId={}", orderFulfill.getOrderId());
            return false;
        }
        return true;
    }

    @Override
    protected void doBizProcess(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill) {
        String fulfillId = request.getFulfillId();
        OrderFulfillOperateTypeEnum operateType = OrderFulfillOperateTypeEnum.DELIVER_ORDER;
        // 更新订单状态
        orderFulfillDAO.updateStatus(fulfillId, operateType.getFromStatus().getCode(), operateType.getToStatus().getCode());
        orderFulfill.setStatus(operateType.getToStatus().getCode());
        orderFulfillLogDAO.save(orderFulfillOperateLogFactory.get(orderFulfill, operateType));

        OrderDeliveredEvent deliveredWmsEvent = (OrderDeliveredEvent) request.getAfterFulfillEvent();
        // 更新配送员信息
        orderFulfillDAO.updateDeliverer(fulfillId, deliveredWmsEvent.getDelivererNo(),
                deliveredWmsEvent.getDelivererName(), deliveredWmsEvent.getDelivererPhone());
    }

    @Override
    protected boolean orderNeedSendMsg(TriggerOrderAfterFulfillEventRequest request) {
        String orderId = request.getOrderId();
        // 订单只需要发送一次已配送消息
        // 预售单在创建履约单的时候可能会发生拆的单情况，即创建多个履约单，
        // 每个履约单进行履约调度的时候都会尝试发送订单履约后的消息，如果两个履约单都属于同一笔订单的话，
        // 那么只有在第一笔履约单进行履约调度单时候，会发送消息

        List<OrderFulfillLogDO> logs = orderFulfillLogDAO.listBy(orderId, OrderFulfillStatusEnum.DELIVERY.getCode());
        if (logs.size() <= 1) {
            // 由于doBizProcess()会先执行，所以至少会插入一条日志
            return true;
        }
        return false;
    }

    @Override
    protected String buildMsgBody(TriggerOrderAfterFulfillEventRequest request) {
        String orderId = request.getOrderId();
        //订单已配送事件
        OrderDeliveredEvent deliveredWmsEvent = (OrderDeliveredEvent) request.getAfterFulfillEvent();
        deliveredWmsEvent.setOrderId(orderId);

        //构建订单已配送消息体
        OrderEvent<OrderDeliveredEvent> orderEvent = buildOrderEvent(orderId, OrderStatusChangeEnum.ORDER_DELIVERED,
                deliveredWmsEvent);
        return JSONObject.toJSONString(orderEvent);
    }
}
