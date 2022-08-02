package com.ruyuan.eshop.fulfill.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.message.OrderEvent;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillLogDO;
import com.ruyuan.eshop.fulfill.domain.event.OrderOutStockEvent;
import com.ruyuan.eshop.fulfill.domain.request.TriggerOrderAfterFulfillEventRequest;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillOperateTypeEnum;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单已出库事件处理器
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class OrderOutStockEventProcessor extends AbstractAfterFulfillEventProcessor {

    @Override
    protected boolean checkFulfillStatus(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill) {
        if (!OrderFulfillStatusEnum.FULFILL.getCode().equals(orderFulfill.getStatus())) {
            log.info("履约单无法出库！orderId={}", orderFulfill.getOrderId());
            return false;
        }
        return true;
    }


    @Override
    protected void doBizProcess(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill) {
        String fulfillId = request.getFulfillId();
        OrderFulfillOperateTypeEnum operateType = OrderFulfillOperateTypeEnum.OUT_STOCK_ORDER;
        // 更新订单状态
        orderFulfillDAO.updateStatus(fulfillId, operateType.getFromStatus().getCode(), operateType.getToStatus().getCode());
        orderFulfill.setStatus(operateType.getToStatus().getCode());
        orderFulfillLogDAO.save(orderFulfillOperateLogFactory.get(orderFulfill, operateType));

    }

    @Override
    protected boolean orderNeedSendMsg(TriggerOrderAfterFulfillEventRequest request) {
        String orderId = request.getOrderId();
        // 订单只需要发送一次已出库的消息
        // 预售单在创建履约单的时候可能会发生拆的单情况，即创建多个履约单，
        // 每个履约单进行履约调度的时候都会尝试发送订单履约后的消息，如果两个履约单都属于同一笔订单的话，
        // 那么只有在第一笔履约单进行履约调度单时候，会发送消息

        List<OrderFulfillLogDO> logs = orderFulfillLogDAO.listBy(orderId, OrderFulfillStatusEnum.DELIVERY.getCode());
        // 由于doBizProcess()会先执行，所以至少会插入一条日志
        return logs.size() <= 1;
    }

    @Override
    protected String buildMsgBody(TriggerOrderAfterFulfillEventRequest request) {
        String orderId = request.getOrderId();
        //订单已出库事件
        OrderOutStockEvent outStockEvent = (OrderOutStockEvent) request.getAfterFulfillEvent();
        outStockEvent.setOrderId(orderId);

        //构建订单已出库消息体
        OrderEvent<OrderOutStockEvent> orderEvent = buildOrderEvent(orderId, OrderStatusChangeEnum.ORDER_OUT_STOCKED,
                outStockEvent);

        return JSONObject.toJSONString(orderEvent);
    }
}
