package moe.ahao.commerce.aftersale.adapter.mq;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.common.infrastructure.event.CancelOrderRefundEvent;
import moe.ahao.commerce.common.infrastructure.event.ReleaseAssetsEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.AbstractMessageListenerConcurrently;
import moe.ahao.commerce.inventory.api.event.ReleaseProductStockEvent;
import moe.ahao.commerce.market.api.command.ReleaseUserCouponEvent;
import moe.ahao.commerce.order.infrastructure.publisher.DefaultProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderItemDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderItemMapper;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听 释放资产消息
 */
@Slf4j
@Component
public class ReleaseAssetsListener extends AbstractMessageListenerConcurrently {
    @Autowired
    private DefaultProducer defaultProducer;

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                // 1、消费到释放资产message
                String message = new String(messageExt.getBody());
                log.info("接收到释放资产消息:{}", message);
                ReleaseAssetsEvent event = JSONHelper.parse(message, ReleaseAssetsEvent.class);

                String orderId = event.getOrderId();
                OrderInfoDO orderInfo = orderInfoMapper.selectOneByOrderId(orderId);
                if(orderInfo == null) {
                    log.info("释放资产消息, 订单查询失败, 跳过不处理:{}", message);
                    continue;
                }

                // 2、发送取消订单退款请求MQ
                this.sendRefundEvent(orderInfo);

                // 3、发送释放库存MQ
                this.sendReleaseProductStockEvent(orderInfo);

                // 4、发送释放优惠券MQ
                this.sendReleaseUserCouponEvent(orderInfo);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private void sendRefundEvent(OrderInfoDO orderInfo) {
        // 已支付订单才能发起退款
        if (orderInfo.getOrderStatus() <= OrderStatusEnum.CREATED.getCode()) {
            return;
        }
        String orderId = orderInfo.getOrderId();

        CancelOrderRefundEvent event = new CancelOrderRefundEvent();
        event.setOrderId(orderId);

        String topic = RocketMqConstant.CANCEL_REFUND_REQUEST_TOPIC;
        String json = JSONHelper.toString(event);
        defaultProducer.sendMessage(topic, json, "取消订单退款", null, orderId);
    }

    /**
     * 释放库存数据
     */
    private void sendReleaseProductStockEvent(OrderInfoDO orderInfo) {
        String orderId = orderInfo.getOrderId();
        List<OrderItemDO> orderItemDOList = orderItemMapper.selectListByOrderId(orderId);

        //  查询订单条目
        List<ReleaseProductStockEvent.OrderItem> orderItemRequestList = new ArrayList<>();
        for (OrderItemDO orderItemDO : orderItemDOList) {
            ReleaseProductStockEvent.OrderItem orderItemRequest = new ReleaseProductStockEvent.OrderItem();
            orderItemRequest.setSkuCode(orderItemDO.getSkuCode());
            orderItemRequest.setSaleQuantity(orderItemDO.getSaleQuantity());

            orderItemRequestList.add(orderItemRequest);
        }

        ReleaseProductStockEvent event = new ReleaseProductStockEvent();
        event.setOrderId(orderId);
        event.setOrderItems(orderItemRequestList);

        String topic = RocketMqConstant.CANCEL_RELEASE_INVENTORY_TOPIC;
        String json = JSONHelper.toString(event);
        defaultProducer.sendMessage(topic, json, "取消订单释放库存", null, orderId);
    }

    /**
     * 释放优惠券数据
     */
    private void sendReleaseUserCouponEvent(OrderInfoDO orderInfo) {
        String couponId = orderInfo.getCouponId();
        String userId = orderInfo.getUserId();
        String orderId = orderInfo.getOrderId();
        if (StringUtils.isEmpty(couponId)) {
            log.info("释放资产消息, 消费券Id为空, 跳过不处理, orderId:{}", orderId);
            return;
        }
        ReleaseUserCouponEvent event = new ReleaseUserCouponEvent();
        event.setCouponId(couponId);
        event.setUserId(userId);
        event.setOrderId(orderId);

        String topic = RocketMqConstant.CANCEL_RELEASE_PROPERTY_TOPIC;
        String json = JSONHelper.toString(event);
        defaultProducer.sendMessage(topic, json, "取消订单释放优惠券", null, orderId);
    }
}
