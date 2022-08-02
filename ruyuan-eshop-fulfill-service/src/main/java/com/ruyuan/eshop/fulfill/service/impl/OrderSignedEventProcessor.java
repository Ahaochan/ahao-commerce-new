package com.ruyuan.eshop.fulfill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.message.OrderEvent;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillLogDO;
import com.ruyuan.eshop.fulfill.domain.event.OrderSignedEvent;
import com.ruyuan.eshop.fulfill.domain.request.TriggerOrderAfterFulfillEventRequest;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillOperateTypeEnum;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单已签收事件处理器
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class OrderSignedEventProcessor extends AbstractAfterFulfillEventProcessor {

    @Override
    protected boolean checkFulfillStatus(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill) {

        if (!OrderFulfillStatusEnum.DELIVERY.getCode().equals(orderFulfill.getStatus())) {
            log.info("履约单无法签收！orderId={}", orderFulfill.getOrderId());
            return false;
        }

        return true;
    }

    @Override
    protected void doBizProcess(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill) {
        String fulfillId = request.getFulfillId();
        OrderFulfillOperateTypeEnum operateType = OrderFulfillOperateTypeEnum.SIGN_ORDER;
        // 更新订单状态
        orderFulfillDAO.updateStatus(fulfillId, operateType.getFromStatus().getCode(), operateType.getToStatus().getCode());
        orderFulfill.setStatus(operateType.getToStatus().getCode());
        orderFulfillLogDAO.save(orderFulfillOperateLogFactory.get(orderFulfill, operateType));
    }

    @Override
    protected boolean orderNeedSendMsg(TriggerOrderAfterFulfillEventRequest request) {
        String orderId = request.getOrderId();
        // 订单只需要发送一次已签收消息
        // 预售单在创建履约单的时候可能会发生拆的单情况，即创建多个履约单，
        // 每个履约单进行履约调度的时候都会尝试发送订单履约后的消息，如果两个履约单都属于同一笔订单的话，
        // 那么只有在最后一笔履约单进行履约调度单时候，会发送消息

        // 查询该订单下所有的履约单
        List<OrderFulfillDO> list = orderFulfillDAO.listByOrderId(orderId);
        List<OrderFulfillLogDO> logs = orderFulfillLogDAO.listBy(orderId, OrderFulfillStatusEnum.DELIVERY.getCode());
        // 只要日志数 >= 该订单下所有的履约单数，说明就是最后一笔履约单进行履约调度了
        return logs.size() >= list.size();
    }

    @Override
    protected String buildMsgBody(TriggerOrderAfterFulfillEventRequest request) {
        String orderId = request.getOrderId();
        //订单已签收事件
        OrderSignedEvent signedWmsEvent = (OrderSignedEvent) request.getAfterFulfillEvent();
        signedWmsEvent.setOrderId(orderId);

        //构建订单已签收消息体
        OrderEvent<OrderSignedEvent> orderEvent = buildOrderEvent(orderId, OrderStatusChangeEnum.ORDER_SIGNED,
                signedWmsEvent);

        return JSONObject.toJSONString(orderEvent);
    }
}
