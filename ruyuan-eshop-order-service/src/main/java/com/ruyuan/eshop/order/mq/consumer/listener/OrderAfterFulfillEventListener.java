package com.ruyuan.eshop.order.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.exception.BaseBizException;
import com.ruyuan.eshop.common.message.OrderEvent;
import com.ruyuan.eshop.common.redis.RedisLock;
import com.ruyuan.eshop.fulfill.domain.event.OrderDeliveredEvent;
import com.ruyuan.eshop.fulfill.domain.event.OrderOutStockEvent;
import com.ruyuan.eshop.fulfill.domain.event.OrderSignedEvent;
import com.ruyuan.eshop.order.converter.AfterFulfillDtoConverter;
import com.ruyuan.eshop.order.domain.dto.AfterFulfillDTO;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.mq.consumer.AbstractRocketMqListener;
import com.ruyuan.eshop.order.service.OrderFulFillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 监听 订单履约后事件消息
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMqConstant.ORDER_WMS_SHIP_RESULT_TOPIC,
        consumerGroup = RocketMqConstant.ORDER_WMS_SHIP_RESULT_CONSUMER_GROUP,
        selectorExpression = "*",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        consumeThreadMax = 1
)
public class OrderAfterFulfillEventListener extends AbstractRocketMqListener {

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private OrderFulFillService orderFulFillService;

    @Autowired
    @Qualifier("afterFulfillDtoConverterImpl")
    private AfterFulfillDtoConverter afterFulfillDtoConverter;

    @Override
    public void onMessage(String message) {
        OrderEvent<?> orderEvent;
        log.info("监听到订单履约后消息：message={}", message);
        orderEvent = JSONObject.parseObject(message, OrderEvent.class);

        //1、解析消息
        AfterFulfillDTO afterFulfillDTO = buildAfterFulfillDTO(orderEvent);

        //2、加分布式锁+里面的前置状态校验防止消息重复消费
        String key = RedisLockKeyConstants.ORDER_AFTER_FULFILL_EVENT_KEY + afterFulfillDTO.getOrderId();
        boolean lock = redisLock.tryLock(key);
        if (!lock) {
            log.error("order has not acquired lock，cannot inform order after fulfill result, orderId={}", afterFulfillDTO.getOrderId());
            throw new BaseBizException(OrderErrorCodeEnum.ORDER_NOT_ALLOW_INFORM_WMS_RESULT);
        }

        //3、通知订单物流结果
        //  注意这里分布式锁加锁放在了本地事务外面
        try {
            orderFulFillService.informOrderAfterFulfillResult(afterFulfillDTO);
        } finally {
            redisLock.unlock(key);
        }
    }

    private AfterFulfillDTO buildAfterFulfillDTO(OrderEvent<?> orderEvent) {
        String messageContent = JSONObject.toJSONString(orderEvent.getMessageContent());
        AfterFulfillDTO afterFulfillDTO = null;
        if (OrderStatusChangeEnum.ORDER_OUT_STOCKED.equals(orderEvent.getOrderStatusChange())) {
            //订单已出库消息
            OrderOutStockEvent outStockWmsEvent = JSONObject.parseObject(messageContent, OrderOutStockEvent.class);
            afterFulfillDTO = afterFulfillDtoConverter.convert(outStockWmsEvent);
        } else if (OrderStatusChangeEnum.ORDER_DELIVERED.equals(orderEvent.getOrderStatusChange())) {
            //订单已配送消息
            OrderDeliveredEvent deliveredWmsEvent = JSONObject.parseObject(messageContent, OrderDeliveredEvent.class);
            afterFulfillDTO = afterFulfillDtoConverter.convert(deliveredWmsEvent);
        } else if (OrderStatusChangeEnum.ORDER_SIGNED.equals(orderEvent.getOrderStatusChange())) {
            //订单已签收消息
            OrderSignedEvent signedWmsEvent = JSONObject.parseObject(messageContent, OrderSignedEvent.class);
            afterFulfillDTO = afterFulfillDtoConverter.convert(signedWmsEvent);
        }
        if (afterFulfillDTO != null) {
            afterFulfillDTO.setStatusChange(orderEvent.getOrderStatusChange());
        }
        return afterFulfillDTO;
    }

}