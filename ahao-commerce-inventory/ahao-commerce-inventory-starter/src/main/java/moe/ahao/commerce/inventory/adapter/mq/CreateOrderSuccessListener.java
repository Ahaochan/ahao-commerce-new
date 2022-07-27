package moe.ahao.commerce.inventory.adapter.mq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.infrastructure.rocketmq.AbstractMessageListenerConcurrently;
import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.commerce.inventory.application.DeductProductStockAppService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监听订单创建成功后的消息
 */
@Slf4j
@Component
public class CreateOrderSuccessListener extends AbstractMessageListenerConcurrently {
    /**
     * 库存服务
     */
    @Autowired
    private DeductProductStockAppService deductProductStockAppService;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String message = new String(messageExt.getBody());
                DeductProductStockCommand command = JSON.parseObject(message, DeductProductStockCommand.class);
                // 触发扣减商品库存
                deductProductStockAppService.deduct(command);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            //本地业务逻辑执行失败，触发消息重新消费
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
