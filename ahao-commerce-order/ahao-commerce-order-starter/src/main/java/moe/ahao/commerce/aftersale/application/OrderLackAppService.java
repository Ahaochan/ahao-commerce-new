package moe.ahao.commerce.aftersale.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.LackCommand;
import moe.ahao.commerce.aftersale.api.dto.LackDTO;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleApplySourceEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.RefundStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleItemDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleRefundDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleInfoMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleRefundMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.service.AfterSaleItemMybatisService;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.enums.AfterSaleTypeDetailEnum;
import moe.ahao.commerce.common.enums.AfterSaleTypeEnum;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.common.event.ActualRefundEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.MQMessage;
import moe.ahao.commerce.order.api.command.GenOrderIdCommand;
import moe.ahao.commerce.order.application.GenOrderIdAppService;
import moe.ahao.commerce.order.infrastructure.domain.dto.OrderExtJsonDTO;
import moe.ahao.commerce.order.infrastructure.domain.dto.OrderLackInfo;
import moe.ahao.commerce.order.infrastructure.domain.dto.OrderLackInfoDTO;
import moe.ahao.commerce.order.infrastructure.enums.AccountTypeEnum;
import moe.ahao.commerce.order.infrastructure.enums.OrderIdTypeEnum;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.gateway.ProductGateway;
import moe.ahao.commerce.order.infrastructure.publisher.LackItemProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderItemDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderItemMapper;
import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.GetProductSkuQuery;
import moe.ahao.exception.BizException;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * ??????????????????service
 */
@Service
@Slf4j
public class OrderLackAppService {
    @Autowired
    private OrderLackAppService _this;
    @Autowired
    private GenOrderIdAppService genOrderIdAppService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private AfterSaleInfoMapper afterSaleInfoMapper;
    @Autowired
    private AfterSaleItemMybatisService afterSaleItemMybatisService;
    @Autowired
    private AfterSaleRefundMapper afterSaleRefundMapper;

    @Autowired
    private ProductGateway productGateway;

    @Autowired
    private LackItemProducer lackItemProducer;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * ?????????????????????????????????
     */
    public boolean isOrderLacked(OrderInfoDO orderInfo) {
        OrderExtJsonDTO orderExtJson = JSONHelper.parse(orderInfo.getExtJson(), OrderExtJsonDTO.class);
        if (null != orderExtJson) {
            return orderExtJson.getLackFlag();
        }
        return false;
    }

    /**
     * ?????????????????????
     */
    public LackDTO execute(LackCommand command) {
        // 1. ??????????????????
        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw OrderExceptionEnum.ORDER_ID_IS_NULL.msg();
        }
        if (CollectionUtils.isEmpty(command.getLackItems())) {
            throw OrderExceptionEnum.LACK_ITEM_IS_NULL.msg();
        }

        // 2. ????????????????????????
        String lockKey = RedisLockKeyConstants.LACK_REQUEST_KEY + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw OrderExceptionEnum.ORDER_NOT_ALLOW_TO_LACK.msg();
        }

        try {
            // 3. ??????????????????
            LackDTO lackDTO = ((OrderLackAppService) AopContext.currentProxy()).doExecute(command);
            return lackDTO;
        } finally {
            lock.unlock();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public LackDTO doExecute(LackCommand command) {
        String orderId = command.getOrderId();
        // 1. ????????????
        OrderInfoDO orderInfo = orderInfoMapper.selectOneByOrderId(orderId);
        if (orderInfo == null) {
            throw OrderExceptionEnum.ORDER_NOT_FOUND.msg();
        }
        List<OrderItemDO> orderItems = orderItemMapper.selectListByOrderId(orderId);
        if (CollectionUtils.isEmpty(orderItems)) {
            throw OrderExceptionEnum.ORDER_ITEM_IS_NULL.msg();
        }
        // ????????????????????????????????????, ?????????????????????????????????:
        // 1.1. ?????????????????????"?????????"
        // 1.2. ????????????????????????
        // ?????????????????????"?????????"????????????????????????:
        // ?????????????????????????????????, ??????????????????, ??????????????????, ?????????????????????????????????????????????????????????????????????, ???"??????"???
        // ??????????????????????????????????????????????????????????????????"?????????", ??????????????????????????????????????????
        boolean orderLacked = this.isOrderLacked(orderInfo);
        boolean canLack = OrderStatusEnum.canLack().contains(orderInfo.getOrderStatus());
        if (!canLack || orderLacked) {
            throw OrderExceptionEnum.ORDER_NOT_ALLOW_TO_LACK.msg();
        }

        // 2. ?????????????????????
        AfterSaleInfoDO lackAfterSaleInfo = this.buildLackAfterSaleInfo(orderInfo);

        // 3. ???????????????????????????
        String afterSaleId = lackAfterSaleInfo.getAfterSaleId();
        List<AfterSaleItemDO> lackAfterSaleItem = this.buildLackAfterSaleItem(afterSaleId, orderItems, command.getLackItems());

        // 4. ?????????????????????, ?????????????????????????????????????????????
        BigDecimal applyRefundAmount = BigDecimal.ZERO;
        BigDecimal realRefundAmount = BigDecimal.ZERO;
        for (AfterSaleItemDO afterSaleItem : lackAfterSaleItem) {
            applyRefundAmount = applyRefundAmount.add(afterSaleItem.getApplyRefundAmount());
            realRefundAmount = realRefundAmount.add(afterSaleItem.getRealRefundAmount());
        }
        lackAfterSaleInfo.setApplyRefundAmount(applyRefundAmount);
        lackAfterSaleInfo.setRealRefundAmount(realRefundAmount);

        // 5. ?????????????????????
        AfterSaleRefundDO afterSaleRefund = this.buildLackAfterSaleRefund(orderInfo, lackAfterSaleInfo);

        // 6. ??????????????????????????????
        OrderExtJsonDTO lackExtJson = this.buildOrderLackExtJson(command, lackAfterSaleInfo);

        //6???????????????????????????
        OrderLackInfo orderLackInfo = OrderLackInfo.builder()
            .lackAfterSaleOrder(lackAfterSaleInfo)
            .afterSaleItems(lackAfterSaleItem)
            .afterSaleRefund(afterSaleRefund)
            .lackExtJson(lackExtJson)
            .orderId(orderInfo.getOrderId())
            .build();


        // 7. ????????????????????????, ??????????????????
        this.sendLackItemEvent(orderInfo.getOrderId(), afterSaleId, orderLackInfo);

        return new LackDTO(orderInfo.getOrderId(), afterSaleId);
    }

    /**
     * ?????????????????????
     */
    private AfterSaleInfoDO buildLackAfterSaleInfo(OrderInfoDO order) {
        // 1. ??????????????????
        String userId = order.getUserId();
        GenOrderIdCommand command = new GenOrderIdCommand(order.getBusinessIdentifier(), OrderIdTypeEnum.AFTER_SALE.getCode(), userId);
        String afterSaleId = genOrderIdAppService.generate(command);

        // 2. ???????????????
        AfterSaleInfoDO afterSaleInfoDO = new AfterSaleInfoDO();
        afterSaleInfoDO.setAfterSaleId(afterSaleId);
        afterSaleInfoDO.setBusinessIdentifier(order.getBusinessIdentifier());
        afterSaleInfoDO.setOrderId(order.getOrderId());
        afterSaleInfoDO.setUserId(userId);
        afterSaleInfoDO.setOrderType(order.getOrderType());
        // ???????????????????????????????????????
        afterSaleInfoDO.setApplySource(AfterSaleApplySourceEnum.SYSTEM.getCode());
        afterSaleInfoDO.setApplyTime(new Date());
        // afterSaleInfoDO.setApplyReasonCode();
        // afterSaleInfoDO.setApplyReason();
        afterSaleInfoDO.setReviewTime(new Date());
        // afterSaleInfoDO.setReviewSource();
        // afterSaleInfoDO.setReviewReasonCode();
        // afterSaleInfoDO.setReviewReason();
        // ???????????????????????????, ???????????????, ??????????????????
        afterSaleInfoDO.setAfterSaleType(AfterSaleTypeEnum.RETURN_MONEY.getCode());
        afterSaleInfoDO.setAfterSaleTypeDetail(AfterSaleTypeDetailEnum.LACK_REFUND.getCode());
        afterSaleInfoDO.setAfterSaleStatus(AfterSaleStatusEnum.REVIEW_PASS.getCode());
        // ?????????????????? ??? ?????????????????? ???????????????
        // afterSaleInfoDO.setApplyRefundAmount();
        // afterSaleInfoDO.setRealRefundAmount();
        // afterSaleInfoDO.setRemark();

        return afterSaleInfoDO;
    }

    /**
     * ?????????????????????????????????
     */
    private List<AfterSaleItemDO> buildLackAfterSaleItem(String afterSaleId, List<OrderItemDO> orderItems, Set<LackCommand.LackItem> lackItems) {
        Map<String, OrderItemDO> orderItemMap = orderItems.stream().collect(Collectors.toMap(OrderItemDO::getSkuCode, Function.identity()));

        List<AfterSaleItemDO> list = new ArrayList<>();
        for (LackCommand.LackItem lackItem : lackItems) {
            String skuCode = lackItem.getSkuCode();
            BigDecimal lackNum = lackItem.getLackNum();

            // 1. ????????????
            if (StringUtils.isEmpty(skuCode)) {
                throw OrderExceptionEnum.SKU_CODE_IS_NULL.msg();
            }
            if (lackNum == null || lackNum.compareTo(BigDecimal.ONE) < 0) {
                throw OrderExceptionEnum.LACK_NUM_IS_LT_0.msg();
            }

            // 2. ??????item??????????????????sku item
            OrderItemDO orderItem = orderItemMap.get(skuCode);
            if (orderItem == null) {
                throw OrderExceptionEnum.LACK_ITEM_NOT_IN_ORDER.msg(skuCode);
            }

            // 3. ????????????????????????>=??????????????????
            if (orderItem.getSaleQuantity().compareTo(lackItem.getLackNum()) <= 0) {
                throw OrderExceptionEnum.LACK_NUM_IS_GE_SKU_ORDER_ITEM_SIZE.msg();
            }

            // 4. ????????????sku
            ProductSkuDTO productSku = productGateway.getBySkuCode(new GetProductSkuQuery(skuCode, orderItem.getSellerId()));
            if (productSku == null) {
                throw OrderExceptionEnum.PRODUCT_SKU_CODE_ERROR.msg(skuCode);
            }

            // 5. ?????????????????????
            AfterSaleItemDO afterSaleItemDO = new AfterSaleItemDO();
            afterSaleItemDO.setAfterSaleId(afterSaleId);
            afterSaleItemDO.setOrderId(orderItem.getOrderId());
            afterSaleItemDO.setSkuCode(productSku.getSkuCode());
            afterSaleItemDO.setProductName(productSku.getProductName());
            afterSaleItemDO.setProductImg(orderItem.getProductImg());
            afterSaleItemDO.setReturnQuantity(lackNum);
            // ????????????
            afterSaleItemDO.setOriginAmount(orderItem.getOriginAmount());
            // ?????????????????? = ?????? * ????????????
            BigDecimal applyRefundAmount = orderItem.getSalePrice().multiply(lackNum);
            afterSaleItemDO.setApplyRefundAmount(applyRefundAmount);
            // ?????????????????? = (????????????/????????????) * ???????????????
            BigDecimal realRefundAmount = this.calculateOrderItemLackRealRefundAmount(orderItem, lackNum);
            afterSaleItemDO.setRealRefundAmount(realRefundAmount);

            list.add(afterSaleItemDO);
        }
        return list;
    }

    /**
     * ???????????????????????????
     */
    private AfterSaleRefundDO buildLackAfterSaleRefund(OrderInfoDO order, AfterSaleInfoDO afterSaleInfo) {
        // 1. ???????????????
        AfterSaleRefundDO AfterSaleRefundDO = new AfterSaleRefundDO();
        // AfterSaleRefundDO.setAfterSaleRefundId();
        AfterSaleRefundDO.setAfterSaleId(afterSaleInfo.getAfterSaleId());
        AfterSaleRefundDO.setOrderId(afterSaleInfo.getOrderId());
        // AfterSaleRefundDO.setAfterSaleBatchNo();
        AfterSaleRefundDO.setAccountType(AccountTypeEnum.THIRD.getCode());
        AfterSaleRefundDO.setPayType(order.getPayType());
        AfterSaleRefundDO.setRefundStatus(RefundStatusEnum.UN_REFUND.getCode());
        AfterSaleRefundDO.setRefundAmount(afterSaleInfo.getRealRefundAmount());
        // AfterSaleRefundDO.setRefundPayTime();
        // AfterSaleRefundDO.setOutTradeNo();
        // AfterSaleRefundDO.setRemark();

        return AfterSaleRefundDO;
    }

    /**
     * ??????????????????????????????
     */
    private OrderExtJsonDTO buildOrderLackExtJson(LackCommand command, AfterSaleInfoDO afterSaleInfo) {
        OrderExtJsonDTO orderExtJson = new OrderExtJsonDTO();
        orderExtJson.setLackFlag(true);

        OrderLackInfoDTO lackInfo = new OrderLackInfoDTO();
        lackInfo.setOrderId(afterSaleInfo.getOrderId());
        lackInfo.setApplyRefundAmount(afterSaleInfo.getApplyRefundAmount());
        lackInfo.setRealRefundAmount(afterSaleInfo.getRealRefundAmount());

        List<OrderLackInfoDTO.LackItem> lackItemDTOList = command.getLackItems().stream()
            .map(i -> new OrderLackInfoDTO.LackItem(i.getSkuCode(), i.getLackNum()))
            .collect(Collectors.toList());
        lackInfo.setLackItems(lackItemDTOList);

        orderExtJson.setLackInfo(lackInfo);
        return orderExtJson;
    }

    /**
     * ???????????????????????????
     */
    private void sendLackItemEvent(String orderId, String afterSaleId, OrderLackInfo orderLackInfo) {
        // 1. ??????????????????
        TransactionMQProducer producer = lackItemProducer.getProducer();
        producer.setTransactionListener(this.getTransactionListener());
        try {
            // 2. ???????????????
            ActualRefundEvent actualRefundMessage = new ActualRefundEvent();
            actualRefundMessage.setOrderId(orderId);
            actualRefundMessage.setAfterSaleId(afterSaleId);
            // TODO ???????????????????????????????????????
            actualRefundMessage.setLastReturnGoods(false);

            // 3. ??????????????????
            String topic = RocketMqConstant.ACTUAL_REFUND_TOPIC;
            byte[] body = JSONHelper.toString(actualRefundMessage).getBytes(StandardCharsets.UTF_8);
            Message mq = new MQMessage(topic, null, orderId, body);
            producer.sendMessageInTransaction(mq, orderLackInfo);
        } catch (MQClientException e) {
            log.error("????????????????????????", e);
            throw OrderExceptionEnum.SEND_TRANSACTION_MQ_FAILED.msg();
        }
    }

    /**
     * ????????????mq???????????????
     */
    private TransactionListener getTransactionListener() {
        return new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                try {
                    OrderLackInfo orderLackInfo = (OrderLackInfo) arg;
                    // ??????????????????
                    _this.saveLackInfo(orderLackInfo);
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (BizException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("system error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                // ???????????????????????????????????????
                String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                ActualRefundEvent refundMessage = JSONHelper.parse(body, ActualRefundEvent.class);

                String afterSaleId = refundMessage.getAfterSaleId();
                AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoMapper.selectOneByAfterSaleId(afterSaleId);
                if (afterSaleInfoDO != null) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        };
    }

    /**
     * ??????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveLackInfo(OrderLackInfo orderLackInfo) {
        // 1??????????????????,item????????????;
        afterSaleInfoMapper.insert(orderLackInfo.getLackAfterSaleOrder());
        afterSaleItemMybatisService.saveBatch(orderLackInfo.getAfterSaleItems());
        afterSaleRefundMapper.insert(orderLackInfo.getAfterSaleRefund());
        // 2???????????????????????????
        String json = JSONHelper.toString(orderLackInfo.getLackExtJson());
        orderInfoMapper.updateExtJsonByOrderId(orderLackInfo.getOrderId(), json);
    }

    private BigDecimal calculateOrderItemLackRealRefundAmount(OrderItemDO orderItem, BigDecimal lackNum) {
        BigDecimal rate = lackNum.divide(orderItem.getSaleQuantity(), 6, RoundingMode.HALF_UP);
        // ??????????????????
        BigDecimal itemRefundAmount = orderItem.getPayAmount().multiply(rate).setScale(6, RoundingMode.DOWN);
        return itemRefundAmount;
    }
}
