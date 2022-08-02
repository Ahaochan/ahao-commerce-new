package com.ruyuan.eshop.inventory.mq.consumer;

import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.inventory.mq.consumer.listener.AfterSaleReleaseInventoryListener;
import com.ruyuan.eshop.inventory.mq.consumer.listener.CancelOrderReleaseInventoryListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Configuration
public class ConsumerConfig {

    @Autowired
    private RocketMQProperties rocketMQProperties;

    /**
     * 取消订单释放库存消息消费者
     */
    @Bean("releaseInventoryConsumer")
    public DefaultMQPushConsumer releaseInventoryConsumer(CancelOrderReleaseInventoryListener cancelOrderReleaseInventoryListener)
            throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.RELEASE_INVENTORY_CONSUMER_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.RELEASE_ASSETS_TOPIC, "*");
        consumer.registerMessageListener(cancelOrderReleaseInventoryListener);
        consumer.start();
        return consumer;
    }

    /**
     * 手动售后释放库存消息消费者
     */
    @Bean("afterSaleReleaseInventoryConsumer")
    public DefaultMQPushConsumer afterSaleReleaseInventoryConsumer(AfterSaleReleaseInventoryListener afterSaleReleaseInventoryListener)
            throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.AFTER_SALE_RELEASE_INVENTORY_CONSUMER_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.AFTER_SALE_RELEASE_INVENTORY_TOPIC, "*");
        consumer.registerMessageListener(afterSaleReleaseInventoryListener);
        consumer.start();
        return consumer;
    }
}