package com.ruyuan.eshop.order.mq.producer;

import com.ruyuan.eshop.common.constants.RocketMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 退款订单发送释放优惠券producer组件
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class RefundOrderSendReleaseCouponProducer extends AbstractTransactionProducer {

    @Autowired
    public RefundOrderSendReleaseCouponProducer(RocketMQProperties rocketMQProperties) {
        producer = new TransactionMQProducer(RocketMqConstant.RELEASE_PROPERTY_PRODUCER_GROUP);
        producer.setNamesrvAddr(rocketMQProperties.getNameServer());
        start();
    }
}
