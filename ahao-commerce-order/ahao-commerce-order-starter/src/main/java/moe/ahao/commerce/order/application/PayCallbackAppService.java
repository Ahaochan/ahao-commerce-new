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
     * 支付回调
     * 支付回调有2把分布式锁的原因说明：同一笔订单在同一时间只能支付or取消
     * 不可以同时对一笔订单，既发起支付，又发起取消
     */
    public void payCallback(PayCallbackCommand command) {
        String orderId = command.getOrderId();
        Integer payType = command.getPayType();
        OrderInfoDO orderInfoDO = orderInfoMapper.selectOneByOrderId(orderId);
        OrderPaymentDetailDO orderPaymentDetailDO = orderPaymentDetailMapper.selectOneByOrderId(orderId);

        // 1. 入参检查
        this.check(command, orderInfoDO, orderPaymentDetailDO);

        // 2. 为支付回调操作进行多重分布式锁加锁
        //    加支付分布式锁避免支付系统并发回调
        String orderPayLockKey = RedisLockKeyConstants.ORDER_PAY_KEY + orderId;
        RLock orderPayLock = redissonClient.getLock(orderPayLockKey);
        //    加取消订单分布式锁避免支付和取消订单同时操作同一笔订单
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
            // 3. 幂等性检查, 只有已创建的订单才能进行支付回调
            boolean isCreated = OrderStatusEnum.CREATED.getCode().equals(orderStatus);
            if (!isCreated) {
                this.payCallbackFailure(orderStatus, payStatus, payType, orderPaymentDetailDO, orderInfoDO);
                return;
            }

            // 4. 执行正式的订单支付回调处理
            this.doPayCallback(orderInfoDO);
        } finally {
            // 5. 释放分布式锁
            multiLock.unlock();
        }
    }

    /**
     * 检查订单支付回调接口入参
     */
    private void check(PayCallbackCommand command, OrderInfoDO orderInfoDO, OrderPaymentDetailDO orderPaymentDetailDO) {
        if (command == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // 订单号
        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // 支付金额
        BigDecimal payAmount = command.getPayAmount();
        if (payAmount == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // 支付系统交易流水号
        String outTradeNo = command.getOutTradeNo();
        if (StringUtils.isEmpty(outTradeNo)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // 支付类型
        Integer payType = command.getPayType();
        if (payType == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
        PayTypeEnum payTypeEnum = PayTypeEnum.getByCode(payType);
        if (payTypeEnum == null) {
            throw OrderExceptionEnum.PAY_TYPE_PARAM_ERROR.msg();
        }

        // 商户ID
        String merchantId = command.getMerchantId();
        if (StringUtils.isEmpty(merchantId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // 校验参数
        if (orderInfoDO == null || orderPaymentDetailDO == null) {
            throw OrderExceptionEnum.ORDER_INFO_IS_NULL.msg();
        }
        if (payAmount.compareTo(orderInfoDO.getPayAmount()) != 0) {
            throw OrderExceptionEnum.ORDER_CALLBACK_PAY_AMOUNT_ERROR.msg();
        }
    }

    /**
     * 支付回调异常的时候处理逻辑
     */
    public void payCallbackFailure(Integer orderStatus, Integer payStatus, Integer payType, OrderPaymentDetailDO orderPaymentDetailDO, OrderInfoDO orderInfoDO) {
        // 1. 如果订单状态是取消状态, 可能是支付回调前就取消了订单，也有可能支付回调成功后取消了订单
        if (OrderStatusEnum.CANCELED.getCode().equals(orderStatus)) {
            // 1.2. 如果支付回调时（说明支付系统已经扣了用户的钱）, 订单状态是已取消, 并且支付状态是未支付（说明支付系统还没有完成回调）
            //      说明当时用户在取消订单的时候, 支付系统还没有完成回调. 而支付系统已经扣了用户的钱, 所以现在回调这里要调用一下退款.
            if (PayStatusEnum.UNPAID.getCode().equals(payStatus)) {
                this.executeOrderRefund(orderPaymentDetailDO);
                throw OrderExceptionEnum.ORDER_CANCEL_PAY_CALLBACK_ERROR.msg();
            }
            // 1.3. 如果支付回调时（说明支付系统已经扣了用户的钱）, 订单状态是已取消, 并且支付状态是已支付（说明支付系统已经回调过一次了）
            //      说明当时用户当时在取消订单的时候, 订单已经不是"已创建"状态了, 可能是已支付之后的状态了.
            if (PayStatusEnum.PAID.getCode().equals(payStatus)) {
                boolean isSamePayType = payType.equals(orderPaymentDetailDO.getPayType());
                if (isSamePayType) {
                    // 1.3.1. 如果是相同的支付方式, 说明用户是没有发起重复付款的, 只是重复回调了.
                    //        取消的订单是不会变成已支付的. 这种场景是支付完成后，执行了取消订单的操作，取消订单本身就会进行退款，所以这里不用进行退款
                    throw OrderExceptionEnum.ORDER_CANCEL_PAY_CALLBACK_PAY_TYPE_SAME_ERROR.msg();
                } else {
                    // 1.3.2. 如果不是相同的支付方式, 说明用户更换了不同的支付方式进行了重复付款
                    //        正常的支付方式走上面的逻辑, 这种不同的支付方式需要调用一下退款
                    //        而非同种支付方式的话，说明用户还是更换了不同支付方式进行了多次扣款，所以需要调用一下退款接口
                    this.executeOrderRefund(orderPaymentDetailDO);
                    throw OrderExceptionEnum.ORDER_CANCEL_PAY_CALLBACK_PAY_TYPE_NO_SAME_ERROR.msg();
                }
            }
        } else {
            // 2. 如果订单状态不是取消状态, 那么就是已支付、已履约、已出库、配送中等状态
            if (PayStatusEnum.PAID.getCode().equals(payStatus)) {
                // 2.1. 如果是相同的支付方式, 说明用户是没有发起重复付款的, 只是重复回调了, 做好幂等直接return就好了.
                boolean isSamePayType = payType.equals(orderPaymentDetailDO.getPayType());
                if (isSamePayType) {
                    return;
                }

                // 2.2. 如果不是相同的支付方式, 说明用户还是更换了不同支付方式进行了多次扣款，所以需要调用一下退款接口
                this.executeOrderRefund(orderPaymentDetailDO);
                throw OrderExceptionEnum.ORDER_CANCEL_PAY_CALLBACK_REPEAT_ERROR.msg();
            }
        }
    }

    /**
     * 支付回调成功的时候处理逻辑
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
            log.error("订单发送事务消息失败", e);
            throw OrderExceptionEnum.ORDER_PAY_CALLBACK_SEND_MQ_ERROR.msg();
        }
    }

    /**
     * 发送支付成功消息时，设置事务消息TransactionListener组件
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
                // 检查订单是否是已支付
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
     * 执行订单退款
     */
    private void executeOrderRefund(OrderPaymentDetailDO orderPaymentDetailDO) {
        RefundOrderCommand command = new RefundOrderCommand();
        command.setOrderId(orderPaymentDetailDO.getOrderId());
        command.setRefundAmount(orderPaymentDetailDO.getPayAmount());
        command.setOutTradeNo(orderPaymentDetailDO.getOutTradeNo());

        payGateway.executeRefund(command);
    }
}
