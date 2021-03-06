package moe.ahao.commerce.customer.adapter.mq;

import moe.ahao.commerce.common.constants.RocketMqConstant;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(RocketMQProperties.class)
public class ConsumerConfig {
    @Autowired
    private RocketMQProperties rocketMQProperties;

    /**
     * 客服接收售后申请消费者
     */
    @Bean("afterSaleCustomerAudit")
    public DefaultMQPushConsumer afterSaleCustomerAudit(AfterSaleCustomerAuditTopicListener listener) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.AFTER_SALE_CUSTOMER_AUDIT_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.AFTER_SALE_CUSTOMER_AUDIT_TOPIC, "*");
        consumer.registerMessageListener(listener);
        consumer.start();
        return consumer;
    }
}
