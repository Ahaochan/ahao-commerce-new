package moe.ahao.commerce.aftersale.application;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.infrastructure.component.AfterSaleOperateLogFactory;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusChangeEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleLogDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleRefundDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleInfoMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleLogMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleRefundMapper;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.event.ActualRefundEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.MQMessage;
import moe.ahao.commerce.market.api.command.ReleaseUserCouponEvent;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.gateway.PayGateway;
import moe.ahao.commerce.order.infrastructure.publisher.RefundOrderSendReleaseCouponProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.pay.api.command.RefundOrderCommand;
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
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Service
public class AfterSaleActualRefundAppService {
    @Autowired
    private AfterSaleActualRefundAppService _this;

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private AfterSaleInfoMapper afterSaleInfoMapper;
    @Autowired
    private AfterSaleRefundMapper afterSaleRefundMapper;
    @Autowired
    private AfterSaleLogMapper afterSaleLogMapper;

    @Autowired
    private AfterSaleOperateLogFactory afterSaleOperateLogFactory;

    @Autowired
    private PayGateway payGateway;

    @Autowired
    private RefundOrderSendReleaseCouponProducer refundOrderSendReleaseCouponProducer;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * ????????????
     */
    public Boolean refundMoney(ActualRefundEvent event) {
        String afterSaleId = event.getAfterSaleId();
        String lockKey = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw OrderExceptionEnum.REFUND_MONEY_REPEAT.msg();
        }
        try {
            AfterSaleInfoDO afterSaleInfo = afterSaleInfoMapper.selectOneByAfterSaleId(afterSaleId);
            AfterSaleRefundDO afterSaleRefund = afterSaleRefundMapper.selectOneByAfterSaleId(afterSaleId);

            // 1. ????????????
            RefundOrderCommand command = new RefundOrderCommand();
            command.setOrderId(event.getOrderId());
            command.setAfterSaleId(afterSaleId);
            command.setRefundAmount(afterSaleRefund.getRefundAmount());
            payGateway.executeRefund(command);


            if (!event.isLastReturnGoods()) {
                // 2. ?????? ???????????????????????? ?????? ???????????????????????????, ???????????????????????????????????????
                _this.updateAfterSaleStatus(afterSaleInfo, AfterSaleStatusEnum.REVIEW_PASS.getCode(), AfterSaleStatusEnum.REFUNDING.getCode());
            } else {
                // 3. ?????? ???????????????????????? ??? ???????????????????????????, ????????????????????????????????????, ??????????????????????????????MQ??????
                this.sendReleaseCouponEvent(afterSaleInfo);
            }
            return true;
        } catch (OrderException e) {
            log.error("system error", e);
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void sendReleaseCouponEvent(AfterSaleInfoDO afterSaleInfo) {
        try {
            // 1. ???????????????
            String orderId = afterSaleInfo.getOrderId();
            OrderInfoDO orderInfoDO = orderInfoMapper.selectOneByOrderId(orderId);
            ReleaseUserCouponEvent releaseUserCouponRequest = new ReleaseUserCouponEvent();
            releaseUserCouponRequest.setCouponId(orderInfoDO.getCouponId());
            releaseUserCouponRequest.setUserId(orderInfoDO.getUserId());
            releaseUserCouponRequest.setAfterSaleId(afterSaleInfo.getAfterSaleId());

            // 2. ??????????????????
            TransactionMQProducer transactionMQProducer = refundOrderSendReleaseCouponProducer.getProducer();
            transactionMQProducer.setTransactionListener(this.getTransactionListener());

            // 3. ??????MQ??????
            String topic = RocketMqConstant.CANCEL_RELEASE_PROPERTY_TOPIC;
            String json = JSONHelper.toString(releaseUserCouponRequest);
            Message message = new MQMessage(topic, json.getBytes(StandardCharsets.UTF_8));
            TransactionSendResult result = transactionMQProducer.sendMessageInTransaction(message, afterSaleInfo);
            if (!result.getLocalTransactionState().equals(LocalTransactionState.COMMIT_MESSAGE)) {
                throw OrderExceptionEnum.REFUND_MONEY_RELEASE_COUPON_FAILED.msg();
            }
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            throw OrderExceptionEnum.SEND_TRANSACTION_MQ_FAILED.msg();
        }
    }

    private TransactionListener getTransactionListener() {
        return new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                try {
                    AfterSaleInfoDO afterSaleInfoDO = (AfterSaleInfoDO) arg;
                    // ?????????????????????
                    _this.updateAfterSaleStatus(afterSaleInfoDO, AfterSaleStatusEnum.REVIEW_PASS.getCode(), AfterSaleStatusEnum.REFUNDING.getCode());
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (Exception e) {
                    log.error("system error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                ReleaseUserCouponEvent releaseUserCouponRequest = JSON.parseObject(
                    new String(msg.getBody(), StandardCharsets.UTF_8), ReleaseUserCouponEvent.class);
                // ????????????????????????"?????????"
                AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoMapper.selectOneByAfterSaleId(releaseUserCouponRequest.getAfterSaleId());
                if (Objects.equals(AfterSaleStatusEnum.REFUNDING.getCode(), afterSaleInfoDO.getAfterSaleStatus())) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        };
    }

    /**
     * ?????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAfterSaleStatus(AfterSaleInfoDO afterSaleInfoDO, Integer fromStatus, Integer toStatus) {
        // ?????? ???????????????
        afterSaleInfoMapper.updateAfterSaleStatusByAfterSaleId(afterSaleInfoDO.getAfterSaleId(), fromStatus, toStatus);

        // ?????? ??????????????????
        AfterSaleLogDO afterSaleLogDO = afterSaleOperateLogFactory.get(afterSaleInfoDO, AfterSaleStatusChangeEnum.getBy(fromStatus, toStatus));
        log.info("????????????????????????,????????????:{},fromStatus:{}, toStatus:{}", afterSaleInfoDO.getAfterSaleId(), fromStatus, toStatus);

        afterSaleLogMapper.insert(afterSaleLogDO);
    }
}
