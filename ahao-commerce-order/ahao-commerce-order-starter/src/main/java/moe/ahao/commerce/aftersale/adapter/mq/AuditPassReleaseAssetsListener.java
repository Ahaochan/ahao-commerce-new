package moe.ahao.commerce.aftersale.adapter.mq;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleItemDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleItemMapper;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.event.ActualRefundEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.AbstractMessageListenerConcurrently;
import moe.ahao.commerce.inventory.api.event.ReleaseProductStockEvent;
import moe.ahao.commerce.order.api.command.AfterSaleAuditPassReleaseAssetsEvent;
import moe.ahao.commerce.order.infrastructure.publisher.DefaultProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderItemDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderItemMapper;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 接收客服审核通过后的 监听 释放资产消息
 */
@Slf4j
@Component
public class AuditPassReleaseAssetsListener extends AbstractMessageListenerConcurrently {
    @Autowired
    private DefaultProducer defaultProducer;

    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private AfterSaleItemMapper afterSaleItemMapper;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                // 1. 消费到释放资产message
                String message = new String(messageExt.getBody());
                log.info("接收到客服审核通过后释放资产的消息:{}", message);
                AfterSaleAuditPassReleaseAssetsEvent event = JSONHelper.parse(message, AfterSaleAuditPassReleaseAssetsEvent.class);

                // 2. 发送释放库存MQ
                this.sendReleaseProductStockEvent(event);

                // 3. 发送实际退款
                this.sendRefundEvent(event);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }


    /**
     * 释放库存数据
     */
    private void sendReleaseProductStockEvent(AfterSaleAuditPassReleaseAssetsEvent event) {
        String orderId = event.getOrderId();
        String afterSaleId = event.getAfterSaleId();

        List<ReleaseProductStockEvent.OrderItem> orderItems = new ArrayList<>();
        List<AfterSaleItemDO> afterSaleItems = afterSaleItemMapper.selectListByAfterSaleId(afterSaleId);
        for (AfterSaleItemDO afterSaleItem : afterSaleItems) {
            ReleaseProductStockEvent.OrderItem orderItem = new ReleaseProductStockEvent.OrderItem();
            orderItem.setSkuCode(afterSaleItem.getSkuCode());
            orderItem.setSaleQuantity(afterSaleItem.getReturnQuantity());

            orderItems.add(orderItem);
        }

        ReleaseProductStockEvent releaseProductStockEvent = new ReleaseProductStockEvent();
        releaseProductStockEvent.setOrderId(orderId);
        releaseProductStockEvent.setOrderItems(orderItems);

        String topic = RocketMqConstant.CANCEL_RELEASE_INVENTORY_TOPIC;
        String json = JSONHelper.toString(releaseProductStockEvent);
        defaultProducer.sendMessage(topic, json, "客服审核通过释放库存", null, orderId);
    }

    private void sendRefundEvent(AfterSaleAuditPassReleaseAssetsEvent command) {
        String orderId = command.getOrderId();
        String afterSaleId = command.getAfterSaleId();
        // 实际退款数据
        ActualRefundEvent event = new ActualRefundEvent();
        event.setOrderId(orderId);
        event.setAfterSaleId(afterSaleId);

        /*
            当前版本判断售后条目是否是订单所属的最后一条 业务限制：
            手动售后是整笔条目退, 不是按数量部分退, 比如skuCode=1的条目购买数量5, 就只能直接退5件, 不能只退3件

            这里判断本次售后条目是否属于该订单的最后一个可售后的条目的逻辑是:
            如果 正向下单的订单条目总条数 = 售后已退款成功的订单条目数 + 1 （本次客服审核通过的这笔条目）
            那么 当前这笔审核通过的条目就是整笔订单的最后一条
        */
        List<OrderItemDO> orderItemDOList = orderItemMapper.selectListByOrderId(orderId);
        // 查询售后订单条目表中不包含当前条目的数量
        List<AfterSaleItemDO> afterSaleItemDOList = afterSaleItemMapper.selectListByOrderIdAndExcludeAfterSaleId(orderId, afterSaleId);
        boolean isLastReturnGoods = (orderItemDOList.size() == afterSaleItemDOList.size() + 1);
        event.setLastReturnGoods(isLastReturnGoods);

        defaultProducer.sendMessage(RocketMqConstant.ACTUAL_REFUND_TOPIC, JSONObject.toJSONString(event), "客服审核通过实际退款", null, null);
    }
}
