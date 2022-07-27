package moe.ahao.commerce.aftersale.adapter.mq;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.application.CancelOrderRefundAppService;
import moe.ahao.commerce.common.infrastructure.event.CancelOrderRefundEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.AbstractMessageListenerConcurrently;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CancelOrderRefundListener extends AbstractMessageListenerConcurrently {
    @Autowired
    private CancelOrderRefundAppService cancelOrderRefundAppService;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String message = new String(messageExt.getBody());
                CancelOrderRefundEvent event = JSONHelper.parse(message, CancelOrderRefundEvent.class);
                log.info("接收到取消订单退款消息:{}", message);

                //  执行 取消订单/超时未支付取消 前的操作
                boolean success = cancelOrderRefundAppService.handler(event);
                if (!success) {
                    throw OrderExceptionEnum.CONSUME_MQ_FAILED.msg();
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
