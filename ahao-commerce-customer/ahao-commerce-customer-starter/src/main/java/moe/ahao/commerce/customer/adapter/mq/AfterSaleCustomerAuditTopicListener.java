package moe.ahao.commerce.customer.adapter.mq;

import com.ruyuan.eshop.common.mq.AbstractMessageListenerConcurrently;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.customer.api.command.CustomerReceiveAfterSaleCommand;
import moe.ahao.commerce.customer.application.ReceivableAfterSaleAppService;
import moe.ahao.commerce.customer.infrastructure.exception.CustomerExceptionEnum;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 接收订单系统售后审核申请
 */
@Slf4j
@Component
public class AfterSaleCustomerAuditTopicListener extends AbstractMessageListenerConcurrently {

    @Autowired
    private ReceivableAfterSaleAppService receivableAfterSaleAppService;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String message = new String(messageExt.getBody());
                log.info("AfterSaleCustomerAuditTopicListener message:{}", message);
                CustomerReceiveAfterSaleCommand command = JSONHelper.parse(message, CustomerReceiveAfterSaleCommand.class);
                //  客服接收订单系统的售后申请
                boolean result = receivableAfterSaleAppService.handler(command);
                if (!result) {
                    throw CustomerExceptionEnum.PROCESS_RECEIVE_AFTER_SALE.msg();
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
