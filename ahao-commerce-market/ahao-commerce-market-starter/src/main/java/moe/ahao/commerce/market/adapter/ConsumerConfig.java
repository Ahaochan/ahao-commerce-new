package moe.ahao.commerce.market.adapter;

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
     * 释放资产权益消息消费者
     */
    @Bean("releaseInventoryConsumer")
    public DefaultMQPushConsumer releaseInventoryConsumer(ReleasePropertyListener releasePropertyListener)
            throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.RELEASE_PROPERTY_CONSUMER_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.CANCEL_RELEASE_PROPERTY_TOPIC, "*");
        consumer.registerMessageListener(releasePropertyListener);
        consumer.start();
        return consumer;
    }

    @Bean
    public ReleasePropertyListener releasePropertyListener() {
        return new ReleasePropertyListener();
    }
}
