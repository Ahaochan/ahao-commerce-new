package moe.ahao.commerce.aftersale.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.CancelOrderCommand;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.common.infrastructure.event.ReleaseAssetsEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.MQMessage;
import moe.ahao.commerce.order.infrastructure.enums.BusinessIdentifierEnum;
import moe.ahao.commerce.order.infrastructure.enums.OrderTypeEnum;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.publisher.CancelOrderSendReleaseAssetsProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.exception.CommonBizExceptionEnum;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.commons.lang3.StringUtils;
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
public class CancelOrderAppService {
    @Autowired
    private CancelOrderTxService cancelOrderTxService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private CancelOrderSendReleaseAssetsProducer cancelOrderSendReleaseAssetsProducer;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * ????????????/????????????????????? ??????
     * <p>
     * ???3?????????????????????
     * 1. ????????????????????????????????????????????????????????????
     * 2. ???????????????????????????MQ?????????????????????????????????????????????????????????
     * 3. ?????????????????????????????????30???????????????????????????
     */
    public boolean cancel(CancelOrderCommand command) {
        // 1. ????????????
        this.check(command);

        // 2. ????????????
        String orderId = command.getOrderId();
        String lockKey = RedisLockKeyConstants.CANCEL_KEY + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw OrderExceptionEnum.CANCEL_ORDER_REPEAT.msg();
        }
        try {
            // 3. ??????????????????
            OrderInfoDO orderInfoDO = orderInfoMapper.selectOneByOrderId(orderId);
            if (orderInfoDO == null) {
                throw OrderExceptionEnum.ORDER_NOT_FOUND.msg();
            }
            if (orderInfoDO.getOrderStatus() >= OrderStatusEnum.OUT_STOCK.getCode()) {
                throw OrderExceptionEnum.CURRENT_ORDER_STATUS_CANNOT_CANCEL.msg();
            }

            // 4. ??????????????????????????????MQ
            this.sendReleaseAssetsEvent(command);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * ????????????
     *
     * @param command ??????????????????
     */
    private void check(CancelOrderCommand command) {
        if (command == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // ????????????, ??????????????????????????????????????????, ???????????????????????????????????????
        Integer orderStatus = command.getOrderStatus();
        if (orderStatus == null) {
            throw OrderExceptionEnum.ORDER_STATUS_IS_NULL.msg();
        }
        if (orderStatus.equals(OrderStatusEnum.CANCELED.getCode())) {
            throw OrderExceptionEnum.ORDER_STATUS_CANCELED.msg();
        }
        if (orderStatus >= OrderStatusEnum.OUT_STOCK.getCode()) {
            throw OrderExceptionEnum.ORDER_STATUS_CHANGED.msg();
        }

        // ??????id
        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw OrderExceptionEnum.CANCEL_ORDER_ID_IS_NULL.msg();
        }
        // ???????????????
        Integer businessIdentifier = command.getBusinessIdentifier();
        if (businessIdentifier == null) {
            throw OrderExceptionEnum.BUSINESS_IDENTIFIER_IS_NULL.msg();
        }
        BusinessIdentifierEnum businessIdentifierEnum = BusinessIdentifierEnum.getByCode(businessIdentifier);
        if (businessIdentifierEnum == null) {
            throw OrderExceptionEnum.BUSINESS_IDENTIFIER_ERROR.msg();
        }

        // ??????????????????
        Integer cancelType = command.getCancelType();
        if (cancelType == null) {
            throw OrderExceptionEnum.CANCEL_TYPE_IS_NULL.msg();
        }

        // ??????ID
        String userId = command.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw OrderExceptionEnum.USER_ID_IS_NULL.msg();
        }

        // ????????????
        Integer orderType = command.getOrderType();
        if (orderType == null) {
            throw OrderExceptionEnum.ORDER_TYPE_IS_NULL.msg();
        }
        OrderTypeEnum orderTypeEnum = OrderTypeEnum.getByCode(orderType);
        if (OrderTypeEnum.UNKNOWN == orderTypeEnum) {
            throw OrderExceptionEnum.ORDER_TYPE_ERROR.msg();
        }
    }

    private void sendReleaseAssetsEvent(CancelOrderCommand command) {
        String orderId = command.getOrderId();

        TransactionMQProducer transactionMQProducer = cancelOrderSendReleaseAssetsProducer.getProducer();
        transactionMQProducer.setTransactionListener(this.getTransactionListener());
        try {
            ReleaseAssetsEvent event = new ReleaseAssetsEvent();
            event.setOrderId(orderId);

            String topic = RocketMqConstant.RELEASE_ASSETS_TOPIC;
            Message message = new MQMessage(topic, JSONHelper.toString(event).getBytes(StandardCharsets.UTF_8));
            TransactionSendResult result = transactionMQProducer.sendMessageInTransaction(message, command);
            if (!result.getLocalTransactionState().equals(LocalTransactionState.COMMIT_MESSAGE)) {
                throw OrderExceptionEnum.CANCEL_ORDER_PROCESS_FAILED.msg();
            }
        } catch (Exception e) {
            throw OrderExceptionEnum.SEND_TRANSACTION_MQ_FAILED.msg();
        }

    }

    /**
     * ??????????????????????????????TransactionListener??????
     */
    private TransactionListener getTransactionListener() {
        return new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                try {
                    CancelOrderCommand command = (CancelOrderCommand) o;
                    // 1. ????????????
                    // 2.1. ??????????????????
                    // 2.2. ????????????????????????
                    cancelOrderTxService.cancelFulfillmentAndUpdateOrderStatus(command);
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (Exception e) {
                    log.error("system error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                ReleaseAssetsEvent message = JSONHelper.parse(new String(messageExt.getBody(), StandardCharsets.UTF_8), ReleaseAssetsEvent.class);
                // ????????????????????????????????????"?????????"
                OrderInfoDO orderInfoByDatabase = orderInfoMapper.selectOneByOrderId(message.getOrderId());
                if (OrderStatusEnum.CANCELED.getCode().equals(orderInfoByDatabase.getOrderStatus())) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        };
    }
}
