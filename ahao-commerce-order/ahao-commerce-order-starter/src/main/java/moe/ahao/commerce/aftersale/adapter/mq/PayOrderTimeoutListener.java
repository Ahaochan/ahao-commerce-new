package moe.ahao.commerce.aftersale.adapter.mq;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.CancelOrderCommand;
import moe.ahao.commerce.aftersale.application.CancelOrderAppService;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.common.infrastructure.event.PayOrderTimeoutEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.AbstractMessageListenerConcurrently;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 监听 支付订单超时延迟消息
 */
@Slf4j
@Component
public class PayOrderTimeoutListener extends AbstractMessageListenerConcurrently {
    @Autowired
    private CancelOrderAppService cancelOrderAppService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                String message = new String(messageExt.getBody());
                PayOrderTimeoutEvent event = JSONHelper.parse(message, PayOrderTimeoutEvent.class);

                this.payOrderTimeout(event);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    public void payOrderTimeout(PayOrderTimeoutEvent event) {
        String orderId = event.getOrderId();
        // 1. 查询当前数据库的订单实时状态
        OrderInfoDO orderInfoDO = orderInfoMapper.selectOneByOrderId(orderId);
        if (orderInfoDO == null) {
            log.warn("订单不存在, 不进行超时取消, orderId:{}", orderId);
            return;
        }
        Integer orderStatus = orderInfoDO.getOrderStatus();
        if (!OrderStatusEnum.CREATED.getCode().equals(orderStatus)) {
            log.warn("订单不等于已创建, 不进行超时取消, orderId:{}", orderId);
            return;
        }
        if (new Date().before(orderInfoDO.getExpireTime())) {
            log.warn("订单还未到达支付截止时间, 不进行超时取消, orderId:{}", orderId);
            return;
        }

        // 2. 消费延迟消息，执行关单逻辑
        CancelOrderCommand command = new CancelOrderCommand();
        command.setOrderId(orderId);
        command.setBusinessIdentifier(event.getBusinessIdentifier());
        command.setCancelType(event.getOrderType());
        command.setUserId(event.getUserId());
        command.setOrderType(event.getOrderType());
        command.setOrderStatus(event.getOrderStatus());
        // command.setOldOrderStatus();
        cancelOrderAppService.cancel(command);
        log.info("关闭订单, orderId:{}", orderId);
    }
}
