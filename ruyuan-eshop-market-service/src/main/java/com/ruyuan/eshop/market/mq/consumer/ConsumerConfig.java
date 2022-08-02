package com.ruyuan.eshop.market.mq.consumer;

import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.market.mq.consumer.listener.AfterSaleReleasePropertyListener;
import com.ruyuan.eshop.market.mq.consumer.listener.CancelOrderReleasePropertyListener;
import com.ruyuan.eshop.market.mq.consumer.listener.MemberPointAddListener;
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
     * 取消订单释放优惠券消费者
     */
    @Bean("releaseCouponConsumer")
    public DefaultMQPushConsumer releaseCouponConsumer(CancelOrderReleasePropertyListener cancelOrderReleasePropertyListener)
            throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.RELEASE_PROPERTY_CONSUMER_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.RELEASE_ASSETS_TOPIC, "*");
        consumer.registerMessageListener(cancelOrderReleasePropertyListener);
        consumer.start();
        return consumer;
    }

    /**
     * 手动售后释放优惠券费者
     */
    @Bean("afterSaleReleaseCouponConsumer")
    public DefaultMQPushConsumer afterSaleReleaseCouponConsumer(AfterSaleReleasePropertyListener afterSaleReleasePropertyListener)
            throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.AFTER_SALE_RELEASE_PROPERTY_CONSUMER_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.AFTER_SALE_RELEASE_PROPERTY_TOPIC, "*");
        consumer.registerMessageListener(afterSaleReleasePropertyListener);
        consumer.start();
        return consumer;
    }

    /**
     * 会员积分增加消费者
     */
    @Bean("memberPointAddConsumer")
    public DefaultMQPushConsumer memberPointAddConsumer(MemberPointAddListener memberPointAddListener)
            throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.MARKET_ORDER_STD_CHANGE_EVENT_CONSUMER_GROUP);
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.subscribe(RocketMqConstant.ORDER_STD_CHANGE_EVENT_TOPIC, OrderStatusChangeEnum.ORDER_PAID.getTags() + " || " +
                OrderStatusChangeEnum.SUB_ORDER_PAID.getTags());
        consumer.registerMessageListener(memberPointAddListener);
        consumer.start();
        return consumer;
    }


}