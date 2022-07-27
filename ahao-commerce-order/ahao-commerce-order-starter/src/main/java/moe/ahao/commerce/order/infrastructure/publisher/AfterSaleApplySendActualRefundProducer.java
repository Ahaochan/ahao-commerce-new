package moe.ahao.commerce.order.infrastructure.publisher;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 售后申请流程发送实际退款producer组件
 */
@Slf4j
@Component
public class AfterSaleApplySendActualRefundProducer extends AbstractTransactionProducer{
    @Autowired
    public AfterSaleApplySendActualRefundProducer(RocketMQProperties rocketMQProperties) {
        producer = new TransactionMQProducer(RocketMqConstant.ACTUAL_REFUND_PRODUCER_GROUP);
        producer.setNamesrvAddr(rocketMQProperties.getNameServer());
        start();
    }
}
