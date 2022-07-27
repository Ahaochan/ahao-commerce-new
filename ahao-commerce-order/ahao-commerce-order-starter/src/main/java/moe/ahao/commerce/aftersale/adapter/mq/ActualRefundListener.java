package moe.ahao.commerce.aftersale.adapter.mq;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.application.AfterSaleActualRefundAppService;
import moe.ahao.commerce.common.event.ActualRefundEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.AbstractMessageListenerConcurrently;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ActualRefundListener extends AbstractMessageListenerConcurrently {
    @Autowired
    private AfterSaleActualRefundAppService afterSaleActualRefundAppService;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String message = new String(messageExt.getBody());
                ActualRefundEvent actualRefundMessage = JSONObject.parseObject(message, ActualRefundEvent.class);
                log.info("接收到实际退款消息:{}", message);

                boolean success = afterSaleActualRefundAppService.refundMoney(actualRefundMessage);
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
