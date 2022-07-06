package com.ruyuan.eshop.order.mq.producer;

import com.ruyuan.eshop.common.constants.RocketMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 取消订单流程发送权益资产producer组件
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class CancelOrderSendReleaseAssetsProducer extends AbstractTransactionProducer {

    @Autowired
    public CancelOrderSendReleaseAssetsProducer(RocketMQProperties rocketMQProperties) {
        producer = new TransactionMQProducer(RocketMqConstant.RELEASE_ASSETS_PRODUCER_GROUP);
        producer.setNamesrvAddr(rocketMQProperties.getNameServer());
        start();
    }

}
