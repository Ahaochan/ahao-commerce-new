package moe.ahao.commerce.order.infrastructure.publisher;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 取消订单流程发送前置准备工作producer组件
 */
@Slf4j
@Component
public class CancelOrderSendPreparationProducer extends AbstractTransactionProducer {
    @Autowired
    public CancelOrderSendPreparationProducer(RocketMQProperties rocketMQProperties) {
        producer = new TransactionMQProducer(RocketMqConstant.ACTUAL_REFUND_PRODUCER_GROUP);
        producer.setNamesrvAddr(rocketMQProperties.getNameServer());
        start();
    }
}
