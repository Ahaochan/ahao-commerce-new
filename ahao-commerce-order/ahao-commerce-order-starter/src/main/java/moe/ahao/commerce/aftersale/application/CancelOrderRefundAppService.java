package moe.ahao.commerce.aftersale.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleInfoMapper;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.event.ActualRefundEvent;
import moe.ahao.commerce.common.infrastructure.event.CancelOrderRefundEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.MQMessage;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.publisher.CancelOrderSendPreparationProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class CancelOrderRefundAppService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private CancelOrderRefundTxService afterSaleManager;


    @Autowired
    private AfterSaleInfoMapper afterSaleInfoMapper;

    @Autowired
    private CancelOrderSendPreparationProducer cancelOrderSendPreparationProducer;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 取消订单/超时未支付取消 执行 退款前计算金额、记录售后信息等准备工作
     */
    public boolean handler(CancelOrderRefundEvent event) {
        String orderId = event.getOrderId();
        // 1. 加分布式锁
        String lockKey = RedisLockKeyConstants.REFUND_KEY + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw OrderExceptionEnum.PROCESS_REFUND_REPEAT.msg();
        }
        try {
            // 2. 取消订单发送实际退款事务MQ
            this.sendCancelOrderActualRefundEvent(event);
            return true;
        } finally {
            lock.unlock();
        }
    }

    private void sendCancelOrderActualRefundEvent(CancelOrderRefundEvent cancelOrderRefundEvent) {
        TransactionMQProducer transactionMQProducer = cancelOrderSendPreparationProducer.getProducer();
        transactionMQProducer.setTransactionListener(this.getTransactionListener());

        try {
            ActualRefundEvent actualRefundEvent = new ActualRefundEvent();
            actualRefundEvent.setOrderId(cancelOrderRefundEvent.getOrderId());
            // 取消订单的场景, 默认是全部退款
            actualRefundEvent.setLastReturnGoods(true);
            // 执行本地事务的时候回写id
            // actualRefundEvent.setAfterSaleId();
            // actualRefundEvent.setAfterSaleRefundId();

            String topic = RocketMqConstant.ACTUAL_REFUND_TOPIC;
            String json = JSONHelper.toString(actualRefundEvent);
            Message message = new MQMessage(topic, json.getBytes(StandardCharsets.UTF_8));
            TransactionSendResult result = transactionMQProducer.sendMessageInTransaction(message, new Object[]{
                cancelOrderRefundEvent, actualRefundEvent
            });
            if (!result.getLocalTransactionState().equals(LocalTransactionState.COMMIT_MESSAGE)) {
                throw OrderExceptionEnum.PROCESS_REFUND_FAILED.msg();
            }
        } catch (Exception e) {
            throw OrderExceptionEnum.SEND_TRANSACTION_MQ_FAILED.msg();
        }
    }

    private TransactionListener getTransactionListener() {
        return new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                try {
                    CancelOrderRefundEvent cancelOrderRefundEvent = (CancelOrderRefundEvent) ((Object[]) o)[0];
                    ActualRefundEvent actualRefundEvent = (ActualRefundEvent) ((Object[]) o)[1];
                    // 记录售后单表、售后条目表、售后变更表、售后支付表
                    CancelOrderRefundTxService.CreateCancelOrderAfterSaleDTO dto = afterSaleManager.insertCancelOrderAfterSale(cancelOrderRefundEvent);
                    // 回写id
                    actualRefundEvent.setAfterSaleId(dto.getAfterSaleId());
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (Exception e) {
                    log.error("system error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                ActualRefundEvent message = JSONHelper.parse(new String(messageExt.getBody(), StandardCharsets.UTF_8), ActualRefundEvent.class);
                // 查询售后数据是否插入成功
                AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoMapper.selectOneByAfterSaleId(message.getAfterSaleId());
                if (afterSaleInfoDO != null) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        };
    }
}
