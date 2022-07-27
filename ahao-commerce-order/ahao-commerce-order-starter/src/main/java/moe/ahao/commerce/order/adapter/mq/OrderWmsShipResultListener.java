package moe.ahao.commerce.order.adapter.mq;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.api.event.OrderEvent;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;
import moe.ahao.commerce.common.infrastructure.rocketmq.AbstractMessageListenerConcurrently;
import moe.ahao.commerce.fulfill.api.event.OrderDeliveredWmsEvent;
import moe.ahao.commerce.fulfill.api.event.OrderOutStockWmsEvent;
import moe.ahao.commerce.fulfill.api.event.OrderSignedWmsEvent;
import moe.ahao.commerce.order.application.OrderFulFillService;
import moe.ahao.commerce.order.infrastructure.domain.dto.WmsShipDTO;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监听 订单物流配送结果消息
 */
@Slf4j
@Component
public class OrderWmsShipResultListener extends AbstractMessageListenerConcurrently {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderFulFillService orderFulFillService;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext context) {
        try {
            for (MessageExt messageExt : list) {
                String message = new String(messageExt.getBody());
                log.info("接收订单物流配送消息, message:{}", message);
                OrderEvent orderEvent = JSONHelper.parse(message, OrderEvent.class);

                // 1. 解析消息
                WmsShipDTO wmsShipDTO = this.buildWmsShip(orderEvent);

                // 2. 加分布式锁+里面的前置状态校验防止消息重复消费
                String lockKey = RedisLockKeyConstants.ORDER_WMS_RESULT_KEY + wmsShipDTO.getOrderId();
                RLock lock = redissonClient.getLock(lockKey);
                boolean locked = lock.tryLock();
                if (!locked) {
                    log.error("order has not acquired lock，cannot inform order wms result, orderId={}", wmsShipDTO.getOrderId());
                    throw OrderExceptionEnum.ORDER_NOT_ALLOW_INFORM_WMS_RESULT.msg();
                }

                // 3. 通知订单物流结果
                try {
                    orderFulFillService.informOrderWmsShipResult(wmsShipDTO);
                } finally {
                    lock.unlock();
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            // 处理业务逻辑失败
            log.error("订单物流配送结果消息处理失败", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private WmsShipDTO buildWmsShip(OrderEvent orderEvent) {
        String messageContent = JSONHelper.toString(orderEvent.getMessageContent());
        WmsShipDTO wmsShipDTO = new WmsShipDTO();
        wmsShipDTO.setOrderId(orderEvent.getOrderId());
        wmsShipDTO.setStatusChange(orderEvent.getOrderStatusChange());
        if (OrderStatusChangeEnum.ORDER_OUT_STOCKED.equals(orderEvent.getOrderStatusChange())) {
            // 订单已出库消息
            OrderOutStockWmsEvent outStockWmsEvent = JSONHelper.parse(messageContent, OrderOutStockWmsEvent.class);
            wmsShipDTO.setOutStockTime(outStockWmsEvent.getOutStockTime());
        } else if (OrderStatusChangeEnum.ORDER_DELIVERED.equals(orderEvent.getOrderStatusChange())) {
            // 订单已配送消息
            OrderDeliveredWmsEvent deliveredWmsEvent = JSONHelper.parse(messageContent, OrderDeliveredWmsEvent.class);
            wmsShipDTO.setDelivererNo(deliveredWmsEvent.getDelivererNo());
            wmsShipDTO.setDelivererName(deliveredWmsEvent.getDelivererName());
            wmsShipDTO.setDelivererPhone(deliveredWmsEvent.getDelivererPhone());
        } else if (OrderStatusChangeEnum.ORDER_SIGNED.equals(orderEvent.getOrderStatusChange())) {
            // 订单已签收消息
            OrderSignedWmsEvent signedWmsEvent = JSONHelper.parse(messageContent, OrderSignedWmsEvent.class);
            wmsShipDTO.setSignedTime(signedWmsEvent.getSignedTime());
        }
        return wmsShipDTO;
    }
}
