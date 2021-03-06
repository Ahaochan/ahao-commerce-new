package moe.ahao.commerce.order.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.common.enums.PayTypeEnum;
import moe.ahao.commerce.common.infrastructure.event.PaidOrderSuccessEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.MQMessage;
import moe.ahao.commerce.order.api.command.PayCallbackCommand;
import moe.ahao.commerce.order.infrastructure.enums.PayStatusEnum;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.gateway.PayGateway;
import moe.ahao.commerce.order.infrastructure.publisher.PaidOrderSuccessProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderPaymentDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderPaymentDetailMapper;
import moe.ahao.commerce.pay.api.command.RefundOrderCommand;
import moe.ahao.exception.BizException;
import moe.ahao.exception.CommonBizExceptionEnum;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class PayCallbackAppService {
    @Autowired
    private PayCallbackTxService payCallbackTxService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderPaymentDetailMapper orderPaymentDetailMapper;

    @Autowired
    private PaidOrderSuccessProducer paidOrderSuccessProducer;

    @Autowired
    private PayGateway payGateway;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * ????????????
     * ???????????????2???????????????????????????????????????????????????????????????????????????or??????
     * ??????????????????????????????????????????????????????????????????
     */
    public void payCallback(PayCallbackCommand command) {
        String orderId = command.getOrderId();
        Integer payType = command.getPayType();
        OrderInfoDO orderInfoDO = orderInfoMapper.selectOneByOrderId(orderId);
        OrderPaymentDetailDO orderPaymentDetailDO = orderPaymentDetailMapper.selectOneByOrderId(orderId);

        // 1. ????????????
        this.check(command, orderInfoDO, orderPaymentDetailDO);

        // 2. ???????????????????????????????????????????????????
        //    ???????????????????????????????????????????????????
        String orderPayLockKey = RedisLockKeyConstants.ORDER_PAY_KEY + orderId;
        RLock orderPayLock = redissonClient.getLock(orderPayLockKey);
        //    ?????????????????????????????????????????????????????????????????????????????????
        String cancelOrderLockKey = RedisLockKeyConstants.CANCEL_KEY + orderId;
        RLock cancelOrderLock = redissonClient.getLock(cancelOrderLockKey);
        RLock multiLock = redissonClient.getMultiLock(orderPayLock, cancelOrderLock);
        boolean locked = multiLock.tryLock();
        if (!locked) {
            throw OrderExceptionEnum.ORDER_PAY_CALLBACK_ERROR.msg();
        }

        try {
            Integer orderStatus = orderInfoDO.getOrderStatus();
            Integer payStatus = orderPaymentDetailDO.getPayStatus();
            // 3. ???????????????, ????????????????????????????????????????????????
            boolean isCreated = OrderStatusEnum.CREATED.getCode().equals(orderStatus);
            if (!isCreated) {
                this.payCallbackFailure(orderStatus, payStatus, payType, orderPaymentDetailDO, orderInfoDO);
                return;
            }

            // 4. ???????????????????????????????????????
            this.doPayCallback(orderInfoDO);
        } finally {
            // 5. ??????????????????
            multiLock.unlock();
        }
    }

    /**
     * ????????????????????????????????????
     */
    private void check(PayCallbackCommand command, OrderInfoDO orderInfoDO, OrderPaymentDetailDO orderPaymentDetailDO) {
        if (command == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // ?????????
        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // ????????????
        BigDecimal payAmount = command.getPayAmount();
        if (payAmount == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // ???????????????????????????
        String outTradeNo = command.getOutTradeNo();
        if (StringUtils.isEmpty(outTradeNo)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // ????????????
        Integer payType = command.getPayType();
        if (payType == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
        PayTypeEnum payTypeEnum = PayTypeEnum.getByCode(payType);
        if (payTypeEnum == null) {
            throw OrderExceptionEnum.PAY_TYPE_PARAM_ERROR.msg();
        }

        // ??????ID
        String merchantId = command.getMerchantId();
        if (StringUtils.isEmpty(merchantId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // ????????????
        if (orderInfoDO == null || orderPaymentDetailDO == null) {
            throw OrderExceptionEnum.ORDER_INFO_IS_NULL.msg();
        }
        if (payAmount.compareTo(orderInfoDO.getPayAmount()) != 0) {
            throw OrderExceptionEnum.ORDER_CALLBACK_PAY_AMOUNT_ERROR.msg();
        }
    }

    /**
     * ???????????????????????????????????????
     */
    public void payCallbackFailure(Integer orderStatus, Integer payStatus, Integer payType, OrderPaymentDetailDO orderPaymentDetailDO, OrderInfoDO orderInfoDO) {
        // 1. ?????????????????????????????????, ?????????????????????????????????????????????????????????????????????????????????????????????
        if (OrderStatusEnum.CANCELED.getCode().equals(orderStatus)) {
            // 1.2. ?????????????????????????????????????????????????????????????????????, ????????????????????????, ???????????????????????????????????????????????????????????????????????????
            //      ??????????????????????????????????????????, ?????????????????????????????????. ???????????????????????????????????????, ?????????????????????????????????????????????.
            if (PayStatusEnum.UNPAID.getCode().equals(payStatus)) {
                this.executeOrderRefund(orderPaymentDetailDO);
                throw OrderExceptionEnum.ORDER_CANCEL_PAY_CALLBACK_ERROR.msg();
            }
            // 1.3. ?????????????????????????????????????????????????????????????????????, ????????????????????????, ??????????????????????????????????????????????????????????????????????????????
            //      ????????????????????????????????????????????????, ??????????????????"?????????"?????????, ????????????????????????????????????.
            if (PayStatusEnum.PAID.getCode().equals(payStatus)) {
                boolean isSamePayType = payType.equals(orderPaymentDetailDO.getPayType());
                if (isSamePayType) {
                    // 1.3.1. ??????????????????????????????, ??????????????????????????????????????????, ?????????????????????.
                    //        ??????????????????????????????????????????. ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    throw OrderExceptionEnum.ORDER_CANCEL_PAY_CALLBACK_PAY_TYPE_SAME_ERROR.msg();
                } else {
                    // 1.3.2. ?????????????????????????????????, ???????????????????????????????????????????????????????????????
                    //        ???????????????????????????????????????, ???????????????????????????????????????????????????
                    //        ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    this.executeOrderRefund(orderPaymentDetailDO);
                    throw OrderExceptionEnum.ORDER_CANCEL_PAY_CALLBACK_PAY_TYPE_NO_SAME_ERROR.msg();
                }
            }
        } else {
            // 2. ????????????????????????????????????, ??????????????????????????????????????????????????????????????????
            if (PayStatusEnum.PAID.getCode().equals(payStatus)) {
                // 2.1. ??????????????????????????????, ??????????????????????????????????????????, ?????????????????????, ??????????????????return?????????.
                boolean isSamePayType = payType.equals(orderPaymentDetailDO.getPayType());
                if (isSamePayType) {
                    return;
                }

                // 2.2. ?????????????????????????????????, ?????????????????????????????????????????????????????????????????????????????????????????????????????????
                this.executeOrderRefund(orderPaymentDetailDO);
                throw OrderExceptionEnum.ORDER_CANCEL_PAY_CALLBACK_REPEAT_ERROR.msg();
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private void doPayCallback(OrderInfoDO orderInfoDO) {
        try {
            TransactionMQProducer transactionMQProducer = paidOrderSuccessProducer.getProducer();
            transactionMQProducer.setTransactionListener(this.getPayCallbackTransactionListener());

            String orderId = orderInfoDO.getOrderId();
            PaidOrderSuccessEvent message = new PaidOrderSuccessEvent();
            message.setOrderId(orderId);

            String topic = RocketMqConstant.PAID_ORDER_SUCCESS_TOPIC;
            byte[] body = JSONHelper.toString(message).getBytes(StandardCharsets.UTF_8);
            Message mq = new MQMessage(topic, null, orderId, body);
            TransactionSendResult result = transactionMQProducer.sendMessageInTransaction(mq, orderInfoDO);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                throw OrderExceptionEnum.ORDER_PAY_CALLBACK_SEND_MQ_ERROR.msg();
            }
        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            throw OrderExceptionEnum.ORDER_PAY_CALLBACK_SEND_MQ_ERROR.msg();
        }
    }

    /**
     * ????????????????????????????????????????????????TransactionListener??????
     */
    private TransactionListener getPayCallbackTransactionListener() {
        return new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                try {
                    OrderInfoDO orderInfo = (OrderInfoDO) o;
                    payCallbackTxService.updateOrderStatusWhenPayCallback(orderInfo);
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (BizException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("system error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                String json = new String(messageExt.getBody(), StandardCharsets.UTF_8);
                PaidOrderSuccessEvent message = JSONHelper.parse(json, PaidOrderSuccessEvent.class);
                // ??????????????????????????????
                OrderInfoDO orderInfoDO = orderInfoMapper.selectOneByOrderId(message.getOrderId());
                if (orderInfoDO != null
                    && OrderStatusEnum.PAID.getCode().equals(orderInfoDO.getOrderStatus())) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        };
    }


    /**
     * ??????????????????
     */
    private void executeOrderRefund(OrderPaymentDetailDO orderPaymentDetailDO) {
        RefundOrderCommand command = new RefundOrderCommand();
        command.setOrderId(orderPaymentDetailDO.getOrderId());
        command.setRefundAmount(orderPaymentDetailDO.getPayAmount());
        command.setOutTradeNo(orderPaymentDetailDO.getOutTradeNo());

        payGateway.executeRefund(command);
    }
}
