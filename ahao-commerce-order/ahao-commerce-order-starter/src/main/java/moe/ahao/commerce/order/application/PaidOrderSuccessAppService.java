package moe.ahao.commerce.order.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.common.infrastructure.event.PaidOrderSuccessEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.MQMessage;
import moe.ahao.commerce.fulfill.api.command.ReceiveFulfillCommand;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.publisher.TriggerOrderFulfillProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.exception.BizException;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static moe.ahao.commerce.common.constants.RocketMqConstant.TRIGGER_ORDER_FULFILL_TOPIC;


@Slf4j
@Service
public class PaidOrderSuccessAppService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderFulFillService orderFulFillService;

    @Autowired
    private TriggerOrderFulfillProducer triggerOrderFulfillProducer;

    @Autowired
    private RedissonClient redissonClient;

    public void consumer(PaidOrderSuccessEvent event) {
        // 1. 参数校验
        String orderId = event.getOrderId();
        log.info("消费已支付消息, 触发订单履约，orderId:{}", orderId);
        OrderInfoDO orderInfoDO = orderInfoMapper.selectOneByOrderId(orderId);
        if (orderInfoDO == null) {
            throw OrderExceptionEnum.ORDER_INFO_IS_NULL.msg();
        }

        // 2. 加分布式锁, 结合履约前置状态校验, 防止消息重复消费
        String lockKey = RedisLockKeyConstants.ORDER_FULFILL_KEY + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            log.error("消费已支付消息, 触发订单履约异常, 获取不到分布式锁{}, orderId:{}", lockKey, orderId);
            throw OrderExceptionEnum.ORDER_FULFILL_ERROR.msg();
        }

        try {
            // 3. 进行订单履约逻辑
            TransactionMQProducer producer = triggerOrderFulfillProducer.getProducer();
            producer.setTransactionListener(this.getTransactionListener());

            // 发送触发履约的消息通知履约系统触发订单进行履约
            ReceiveFulfillCommand receiveFulfillRequest = orderFulFillService.buildReceiveFulFillRequest(orderInfoDO);
            String topic = TRIGGER_ORDER_FULFILL_TOPIC;
            byte[] body = JSONHelper.toString(receiveFulfillRequest).getBytes(StandardCharsets.UTF_8);
            Message mq = new MQMessage(topic, null, orderId, body);
            TransactionSendResult transactionSendResult = producer.sendMessageInTransaction(mq, orderInfoDO);
            boolean sendOK = transactionSendResult.getSendStatus().equals(SendStatus.SEND_OK);
            if (!sendOK) {
                throw OrderExceptionEnum.ORDER_FULFILL_ERROR.msg();
            }
        } catch (MQClientException e) {
            log.error("发送触发履约的消息失败", e);
            throw OrderExceptionEnum.ORDER_FULFILL_ERROR.msg();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 设置事务消息回调监听器
     */
    private TransactionListener getTransactionListener() {
        return new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                try {
                    OrderInfoDO orderInfo = (OrderInfoDO) o;
                    orderFulFillService.triggerOrderFulFill(orderInfo.getOrderId());
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (BizException e) {
                    log.error("biz error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                } catch (Exception e) {
                    log.error("system error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                ReceiveFulfillCommand receiveFulfillRequest = JSONHelper.parse(new String(messageExt.getBody(), StandardCharsets.UTF_8), ReceiveFulfillCommand.class);
                // 检查订单是否"已履约"状态
                OrderInfoDO orderInfoDO = orderInfoMapper.selectOneByOrderId(receiveFulfillRequest.getOrderId());
                if (orderInfoDO != null
                    && OrderStatusEnum.FULFILL.getCode().equals(orderInfoDO.getOrderStatus())) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        };
    }
}
