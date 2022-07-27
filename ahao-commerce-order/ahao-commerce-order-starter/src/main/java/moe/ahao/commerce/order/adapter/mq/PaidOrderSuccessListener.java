package moe.ahao.commerce.order.adapter.mq;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.infrastructure.event.PaidOrderSuccessEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.AbstractMessageListenerConcurrently;
import moe.ahao.commerce.order.application.PaidOrderSuccessAppService;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监听订单支付成功后的消息
 */
@Slf4j
@Component
public class PaidOrderSuccessListener extends AbstractMessageListenerConcurrently {
    @Autowired
    private PaidOrderSuccessAppService paidOrderSuccessAppService;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String message = new String(messageExt.getBody());
                PaidOrderSuccessEvent event = JSONHelper.parse(message, PaidOrderSuccessEvent.class);

                paidOrderSuccessAppService.consumer(event);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            // 本地业务逻辑执行失败，触发消息重新消费
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
