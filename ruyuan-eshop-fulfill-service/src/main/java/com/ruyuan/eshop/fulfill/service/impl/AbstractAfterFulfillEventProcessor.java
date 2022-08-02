package com.ruyuan.eshop.fulfill.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.message.OrderEvent;
import com.ruyuan.eshop.common.mq.MQMessage;
import com.ruyuan.eshop.fulfill.dao.OrderFulfillDAO;
import com.ruyuan.eshop.fulfill.dao.OrderFulfillLogDAO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.request.TriggerOrderAfterFulfillEventRequest;
import com.ruyuan.eshop.fulfill.mq.producer.DefaultProducer;
import com.ruyuan.eshop.fulfill.service.OrderAfterFulfillEventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
public abstract class AbstractAfterFulfillEventProcessor implements OrderAfterFulfillEventProcessor {

    @Autowired
    private DefaultProducer defaultProducer;

    @Autowired
    protected OrderFulfillDAO orderFulfillDAO;

    @Autowired
    protected OrderFulfillLogDAO orderFulfillLogDAO;

    @Autowired
    protected OrderFulfillOperateLogFactory orderFulfillOperateLogFactory;

    @Override
    public void execute(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill) {
        if (!checkFulfillStatus(request, orderFulfill)) {
            return;
        }

        //  1、执行业务流程
        doBizProcess(request, orderFulfill);

        // 2、判断是否需要发送订单履约后消息
        if (orderNeedSendMsg(request)) {

            //  3、构造消息体
            String body = buildMsgBody(request);

            //  4、发送消息
            sendMessage(body, request.getOrderId());
        }
    }

    /**
     * 判断是履约状态
     *
     * @param request      请求
     * @param orderFulfill 履约信息
     * @return 结果
     */
    protected abstract boolean checkFulfillStatus(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill);

    /**
     * 判断订单是否需要发送消息
     *
     * @param request 请求
     * @return 结果
     */
    protected abstract boolean orderNeedSendMsg(TriggerOrderAfterFulfillEventRequest request);

    /**
     * 执行业务流程
     */
    protected abstract void doBizProcess(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill);

    protected abstract String buildMsgBody(TriggerOrderAfterFulfillEventRequest request);

    private void sendMessage(String body, String orderId) {
        if (StringUtils.isNotBlank(body)) {
            Message message = new MQMessage();
            message.setTopic(RocketMqConstant.ORDER_WMS_SHIP_RESULT_TOPIC);
            message.setBody(body.getBytes(StandardCharsets.UTF_8));
            try {
                DefaultMQProducer defaultMQProducer = defaultProducer.getProducer();
                SendResult sendResult = defaultMQProducer.send(message, (mqs, message1, arg) -> {
                    //根据订单id选择发送queue
                    String orderId1 = (String) arg;
                    long index = hash(orderId1) % mqs.size();
                    return mqs.get((int) index);
                }, orderId);

                log.info("发送订单履约后消息，SendResult status:{}, queueId:{}, body:{}", sendResult.getSendStatus(),
                        sendResult.getMessageQueue().getQueueId(), body);
            } catch (Exception e) {
                log.error("发送订单履约后消息异常,orderId={},err={}", orderId, e.getMessage(), e);
            }
        }
    }

    protected <T> OrderEvent<T> buildOrderEvent(String orderId, OrderStatusChangeEnum orderStatusChange, T messaheContent) {
        OrderEvent<T> orderEvent = new OrderEvent<>();
        orderEvent.setOrderId(orderId);
        orderEvent.setBusinessIdentifier(1);
        orderEvent.setOrderType(1);
        orderEvent.setOrderStatusChange(orderStatusChange);
        orderEvent.setMessageContent(messaheContent);
        return orderEvent;
    }

    /**
     * hash
     *
     * @param orderId
     * @return
     */
    private int hash(String orderId) {
        //解决取模可能为负数的情况
        return orderId.hashCode() & Integer.MAX_VALUE;
    }


}
