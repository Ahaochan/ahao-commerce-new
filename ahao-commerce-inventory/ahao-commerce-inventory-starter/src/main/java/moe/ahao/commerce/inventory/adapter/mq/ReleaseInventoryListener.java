package moe.ahao.commerce.inventory.adapter.mq;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.mq.AbstractMessageListenerConcurrently;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.inventory.api.command.ReleaseProductStockCommand;
import moe.ahao.commerce.inventory.application.ReleaseProductStockAppService;
import moe.ahao.commerce.inventory.infrastructure.exception.InventoryExceptionEnum;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 监听释放库存消息
 */
@Slf4j
@Component
public class ReleaseInventoryListener extends AbstractMessageListenerConcurrently {

    @Autowired
    private ReleaseProductStockAppService releaseProductStockAppService;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt msg : list) {
                String content = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.info("ReleaseInventoryConsumer message:{}", content);
                ReleaseProductStockCommand command = JSONObject.parseObject(content, ReleaseProductStockCommand.class);
                //  释放库存
                boolean success = releaseProductStockAppService.releaseProductStock(command);
                if (!success) {
                    throw InventoryExceptionEnum.CONSUME_MQ_FAILED.msg();
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
