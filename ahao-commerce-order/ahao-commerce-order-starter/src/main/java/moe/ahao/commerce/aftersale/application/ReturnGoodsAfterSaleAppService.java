package moe.ahao.commerce.aftersale.application;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.CreateReturnGoodsAfterSaleCommand;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleApplySourceEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleReasonEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.RefundStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleItemDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleLogDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleRefundDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleInfoMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleItemMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleLogMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleRefundMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.service.AfterSaleItemMybatisService;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.enums.AfterSaleTypeDetailEnum;
import moe.ahao.commerce.common.enums.AfterSaleTypeEnum;
import moe.ahao.commerce.common.enums.AmountTypeEnum;
import moe.ahao.commerce.common.infrastructure.rocketmq.MQMessage;
import moe.ahao.commerce.customer.api.event.CustomerReceiveAfterSaleEvent;
import moe.ahao.commerce.order.api.command.GenOrderIdCommand;
import moe.ahao.commerce.order.application.GenOrderIdAppService;
import moe.ahao.commerce.order.infrastructure.enums.AccountTypeEnum;
import moe.ahao.commerce.order.infrastructure.enums.BusinessIdentifierEnum;
import moe.ahao.commerce.order.infrastructure.enums.OrderIdTypeEnum;
import moe.ahao.commerce.order.infrastructure.enums.ReturnGoodsTypeEnum;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.publisher.AfterSaleApplySendActualRefundProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderAmountDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderItemDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderPaymentDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderAmountMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderItemMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderPaymentDetailMapper;
import moe.ahao.exception.CommonBizExceptionEnum;
import moe.ahao.util.commons.io.JSONHelper;
import moe.ahao.util.commons.lang.RandomHelper;
import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class ReturnGoodsAfterSaleAppService {
    @Autowired
    @Lazy
    private ReturnGoodsAfterSaleAppService _this;
    @Autowired
    private GenOrderIdAppService genOrderIdAppService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderAmountMapper orderAmountMapper;
    @Autowired
    private OrderPaymentDetailMapper orderPaymentDetailMapper;
    @Autowired
    private AfterSaleInfoMapper afterSaleInfoMapper;
    @Autowired
    private AfterSaleItemMapper afterSaleItemMapper;
    @Autowired
    private AfterSaleItemMybatisService afterSaleItemMybatisService;
    @Autowired
    private AfterSaleLogMapper afterSaleLogMapper;
    @Autowired
    private AfterSaleRefundMapper afterSaleRefundMapper;

    @Autowired
    private AfterSaleApplySendActualRefundProducer afterSaleApplySendActualRefundProducer;

    @Autowired
    private RedissonClient redissonClient;

    public void create(CreateReturnGoodsAfterSaleCommand command) {
        // 1. 参数校验
        this.check1(command);

        // 2. 加分布式锁
        String orderId = command.getOrderId();
        String lockKey = RedisLockKeyConstants.REFUND_KEY + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw OrderExceptionEnum.PROCESS_AFTER_SALE_RETURN_GOODS.msg();
        }
        try {
            // 3. 创建售后单
            _this.doCreate(command);
        } finally {
            // 4. 释放分布式锁
            lock.unlock();
        }
    }

    private void check1(CreateReturnGoodsAfterSaleCommand command) {
        if (command == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw OrderExceptionEnum.ORDER_ID_IS_NULL.msg();
        }

        String userId = command.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw OrderExceptionEnum.USER_ID_IS_NULL.msg();
        }

        Integer businessIdentifier = command.getBusinessIdentifier();
        if (businessIdentifier == null) {
            throw OrderExceptionEnum.BUSINESS_IDENTIFIER_IS_NULL.msg();
        }
        BusinessIdentifierEnum businessIdentifierEnum = BusinessIdentifierEnum.getByCode(businessIdentifier);
        if (businessIdentifierEnum == null) {
            throw OrderExceptionEnum.BUSINESS_IDENTIFIER_ERROR.msg();
        }

        Integer returnGoodsCode = command.getReturnGoodsCode();
        if (returnGoodsCode == null) {
            throw OrderExceptionEnum.RETURN_GOODS_CODE_IS_NULL.msg();
        }

        String skuCode = command.getSkuCode();
        if (StringUtils.isEmpty(skuCode)) {
            throw OrderExceptionEnum.SKU_IS_NULL.msg();
        }
    }

    /**
     * 处理售后申请 入口
     * 当前业务限制说明：
     * 目前业务限定，一笔订单包含多笔订单条目，每次手动售后只能退一笔条目,不支持单笔条目多次退不同数量
     * <p>
     * 举例：
     * 一笔订单包含订单条目A（购买数量10）和订单条目B（购买数量1），每一次可单独发起 售后订单条目A or 售后订单条目B，
     * 如果是售后订单条目A，那么就是把A中购买数量10全部退掉
     * 如果是售后订单条目B，那么就是把B中购买数量1全部退款
     * <p>
     * 暂不支持第一次退A中的3条，第二次退A中的2条，第三次退A中的5条这种退法
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doCreate(CreateReturnGoodsAfterSaleCommand command) {
        // 1. 售后单状态验证   用order id和sku code查到售后id
        String orderId = command.getOrderId();
        String skuCode = command.getSkuCode();
        String userId = command.getUserId();
        this.check2(orderId, skuCode);

        // 2. 计算退货金额
        ReturnGoodsAssembleDTO dto = this.calculateReturnGoodsAmount(orderId, skuCode);

        // 3. 生成售后订单号
        String afterSaleId = genOrderIdAppService.generate(new GenOrderIdCommand(command.getBusinessIdentifier(), OrderIdTypeEnum.AFTER_SALE.getCode(), userId));
        dto.setOrderId(orderId);
        dto.setUserId(userId);
        dto.setAfterSaleId(afterSaleId);

        // 4、执行本地事务, 发送实际退款事务MQ消息
        this.sendActualRefundEvent(dto);
        return true;
    }

    private void check2(String orderId, String skuCode) {
        /*
           场景校验逻辑：
           第一种场景: 订单条目A是第一次发起手动售后，此时售后订单条目表没有该订单的记录，afterSaleItems 是空，正常执行后面的售后逻辑
           第二种场景: 订单条目A已发起过售后，非"撤销成功"状态的售后单不允许重复发起售后
        */
        List<AfterSaleItemDO> afterSaleItems = afterSaleItemMapper.selectListByOrderIdAndSkuCode(orderId, skuCode);
        if (CollectionUtils.isNotEmpty(afterSaleItems)) {
            // TODO 如果当前订单条目sku有处理中的售后单（状态没有到达终态），就不允许重复发起售后

            // TODO 如果发生多次退货，这里只能取到第一次售后单的售后状态做判断，是不是有问题？
            // 看业务需求，如果不希望重新售后，那就不允许，如果要允许那就代码放开限制；
            String afterSaleId = afterSaleItems.get(0).getAfterSaleId();
            AfterSaleInfoDO afterSaleInfo = afterSaleInfoMapper.selectOneByAfterSaleId(afterSaleId);
            // TODO 撤销审核拒绝、退款失败，就不允许重复发起退货售后吗？
            // 多次退货每次得取自己的那一次售后单的状态，确实有点小bug的，自己可以修正
            if (!Objects.equals(AfterSaleStatusEnum.REVOKE.getCode(), afterSaleInfo.getAfterSaleStatus())) {
                // 非"撤销成功"状态的售后单不能重复发起售后
                throw OrderExceptionEnum.PROCESS_APPLY_AFTER_SALE_CANNOT_REPEAT.msg();
            }
        }
    }

    private AfterSaleLogDO buildReturnGoodsAfterSaleLog(String afterSaleId, Integer fromStatus, Integer toStatus) {
        AfterSaleLogDO afterSaleLogDO = new AfterSaleLogDO();
        afterSaleLogDO.setAfterSaleId(afterSaleId);
        afterSaleLogDO.setPreStatus(fromStatus);
        afterSaleLogDO.setCurrentStatus(toStatus);
        afterSaleLogDO.setRemark(ReturnGoodsTypeEnum.AFTER_SALE_RETURN_GOODS.getName());

        return afterSaleLogDO;
    }

    /**
     * 售后退货流程 插入订单销售表
     */
    private AfterSaleInfoDO buildReturnGoodsAfterSaleInfo(OrderInfoDO orderInfo, AfterSaleTypeEnum afterSaleType, String afterSaleId) {
        AfterSaleInfoDO afterSaleInfo = new AfterSaleInfoDO();
        afterSaleInfo.setAfterSaleId(afterSaleId);
        afterSaleInfo.setBusinessIdentifier(orderInfo.getBusinessIdentifier());
        afterSaleInfo.setOrderId(orderInfo.getOrderId());
        afterSaleInfo.setUserId(orderInfo.getUserId());
        afterSaleInfo.setOrderType(orderInfo.getOrderType());
        afterSaleInfo.setApplySource(AfterSaleApplySourceEnum.USER_RETURN_GOODS.getCode());
        afterSaleInfo.setApplyTime(new Date());
        afterSaleInfo.setApplyReasonCode(AfterSaleReasonEnum.USER.getCode());
        afterSaleInfo.setApplyReason(AfterSaleReasonEnum.USER.getName());
        // 审核信息要等客服系统审核后更新
        // afterSaleInfo.setReviewTime();
        // afterSaleInfo.setReviewSource();
        // afterSaleInfo.setReviewReasonCode();
        // afterSaleInfo.setReviewReason();
        if (AfterSaleTypeEnum.RETURN_GOODS == afterSaleType) {
            // 退货流程 只退订单的一笔条目
            afterSaleInfo.setAfterSaleType(AfterSaleTypeEnum.RETURN_GOODS.getCode());
        } else if (AfterSaleTypeEnum.RETURN_MONEY == afterSaleType) {
            // 退货流程 退订单的全部条目 后续按照整笔退款逻辑处理
            afterSaleInfo.setAfterSaleType(AfterSaleTypeEnum.RETURN_MONEY.getCode());
        }
        afterSaleInfo.setAfterSaleTypeDetail(AfterSaleTypeDetailEnum.PART_REFUND.getCode());
        afterSaleInfo.setAfterSaleStatus(AfterSaleStatusEnum.COMMITED.getCode());
        // 申请退款金额和实际退款金额后面进行计算
        // afterSaleInfo.setApplyRefundAmount();
        // afterSaleInfo.setRealRefundAmount();
        afterSaleInfo.setRemark(ReturnGoodsTypeEnum.AFTER_SALE_RETURN_GOODS.getName());
        return afterSaleInfo;
    }

    private void sendActualRefundEvent(ReturnGoodsAssembleDTO dto) {
        try {
            // 1. 执行本地事务
            TransactionMQProducer transactionMQProducer = afterSaleApplySendActualRefundProducer.getProducer();
            transactionMQProducer.setTransactionListener(this.getTransactionListener());

            // 2. 组装发送消息数据
            CustomerReceiveAfterSaleEvent event = new CustomerReceiveAfterSaleEvent();
            event.setUserId(dto.getUserId());
            event.setOrderId(dto.getOrderId());
            event.setAfterSaleId(dto.getAfterSaleId());
            event.setAfterSaleRefundId(this.generateAfterSaleRefundId(dto.getAfterSaleId()));
            event.setAfterSaleType(dto.getAfterSaleTypeEnum().getCode());
            event.setReturnGoodAmount(dto.getReturnGoodAmount());
            event.setApplyRefundAmount(dto.getApplyRefundAmount());

            // 3. 发送事务消息
            String topic = RocketMqConstant.AFTER_SALE_CUSTOMER_AUDIT_TOPIC;
            String json = JSONHelper.toString(event);
            Message message = new MQMessage(topic, json.getBytes(StandardCharsets.UTF_8));
            TransactionSendResult result = transactionMQProducer.sendMessageInTransaction(message, dto);
            if (!result.getLocalTransactionState().equals(LocalTransactionState.COMMIT_MESSAGE)) {
                throw OrderExceptionEnum.SEND_AFTER_SALE_CUSTOMER_AUDIT_MQ_FAILED.msg();
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
                    ReturnGoodsAssembleDTO dto = (ReturnGoodsAssembleDTO) o;
                    // 售后数据落库
                    _this.insertReturnGoodsAfterSale(dto, AfterSaleStatusEnum.COMMITED.getCode());
                    return LocalTransactionState.COMMIT_MESSAGE;
                } catch (Exception e) {
                    log.error("system error", e);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                CustomerReceiveAfterSaleEvent customerReceiveAfterSaleRequest = JSONHelper.parse(new String(messageExt.getBody(), StandardCharsets.UTF_8), CustomerReceiveAfterSaleEvent.class);
                String afterSaleId = customerReceiveAfterSaleRequest.getAfterSaleId();
                // 查询售后数据是否插入成功
                AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoMapper.selectOneByAfterSaleId(afterSaleId);
                if (afterSaleInfoDO != null) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        };
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertReturnGoodsAfterSale(ReturnGoodsAssembleDTO dto, Integer afterSaleStatus) {
        //  售后退货过程中的 申请退款金额 和 实际退款金额 是计算出来的，金额有可能不同
        String orderId = dto.getOrderId();
        String afterSaleId = dto.getAfterSaleId();

        // 1. 新增售后订单表
        OrderInfoDO orderInfo = orderInfoMapper.selectOneByOrderId(orderId);
        AfterSaleInfoDO afterSaleInfo = this.buildReturnGoodsAfterSaleInfo(orderInfo, dto.getAfterSaleTypeEnum(), afterSaleId);
        afterSaleInfoMapper.insert(afterSaleInfo);
        log.info("新增订单售后记录,订单号:{},售后单号:{},订单售后状态:{}", orderId, afterSaleId, afterSaleInfo.getAfterSaleStatus());

        BigDecimal applyRefundAmount = dto.getApplyRefundAmount();
        afterSaleInfo.setApplyRefundAmount(applyRefundAmount);
        BigDecimal returnGoodAmount = dto.getReturnGoodAmount();
        afterSaleInfo.setRealRefundAmount(returnGoodAmount);

        // 2. 新增售后条目表
        List<AfterSaleItemDO> afterSaleItems = this.buildAfterSaleItems(dto.getRefundOrderItems(), afterSaleId);
        afterSaleItemMybatisService.saveBatch(afterSaleItems);

        // 3. 新增售后变更表
        AfterSaleLogDO afterSaleLog = this.buildReturnGoodsAfterSaleLog(afterSaleId, AfterSaleStatusEnum.UN_CREATED.getCode(), afterSaleStatus);
        afterSaleLogMapper.insert(afterSaleLog);
        log.info("新增售后单变更信息, 售后单号:{},状态:PreStatus{},CurrentStatus:{}", afterSaleLog.getAfterSaleId(), afterSaleLog.getPreStatus(), afterSaleLog.getCurrentStatus());

        // 4. 新增售后支付表
        AfterSaleRefundDO afterSaleRefund = this.buildAfterSaleRefund(afterSaleInfo);
        afterSaleRefundMapper.insert(afterSaleRefund);
        log.info("新增售后支付信息, 订单号:{}, 售后单号:{}, 状态:{}", orderId, afterSaleId, afterSaleRefund.getRefundStatus());
    }

    /**
     * 一笔订单只有一个条目：整笔退
     * 一笔订单有多个条目，每次退一条，退完最后一条补退运费和优惠券
     */
    private ReturnGoodsAssembleDTO calculateReturnGoodsAmount(String orderId, String skuCode) {
        List<OrderItemDO> orderItems = orderItemMapper.selectListByOrderId(orderId);
        List<AfterSaleItemDO> afterSaleItems = afterSaleItemMapper.selectListByOrderId(orderId);

        // 找到本次要退的订单条目
        OrderItemDO orderItem = orderItems.stream().filter(i -> Objects.equals(i.getSkuCode(), skuCode)).findFirst().orElse(null);
        if (orderItem == null) {
            throw OrderExceptionEnum.ORDER_ITEM_IS_NULL.msg();
        }

        ReturnGoodsAssembleDTO dto = new ReturnGoodsAssembleDTO();
        int orderItemNum = orderItems.size();
        int afterSaleItemNum = afterSaleItems.size();
        // 该笔订单条目数 = 已存的售后订单条目数 + 本次退货的条目 (每次退 1 条)
        boolean lastReturnGoods = (orderItemNum == afterSaleItemNum + 1);
        if (lastReturnGoods) {
            // 如果当前条目是订单的最后一笔, 就连运费一起退了
            OrderAmountDO deliveryAmount = orderAmountMapper.selectOneByOrderIdAndAmountType(orderId, AmountTypeEnum.SHIPPING_AMOUNT.getCode());
            BigDecimal freightAmount = Optional.ofNullable(deliveryAmount).map(OrderAmountDO::getAmount).orElse(BigDecimal.ZERO);
            // 本次售后类型: 退款
            dto.setAfterSaleTypeEnum(AfterSaleTypeEnum.RETURN_MONEY);
            // 最终退款金额 = 实际退款金额 + 运费
            dto.setReturnGoodAmount(orderItem.getPayAmount().add(freightAmount));
        } else {
            // 如果该笔订单还有其他条目, 本次售后类型: 退货
            dto.setAfterSaleTypeEnum(AfterSaleTypeEnum.RETURN_GOODS);
            dto.setReturnGoodAmount(orderItem.getPayAmount());
        }

        dto.setApplyRefundAmount(orderItem.getOriginAmount());
        dto.setLastReturnGoods(lastReturnGoods);
        // 为便于以后扩展，这里封装成list
        dto.setRefundOrderItems(Arrays.asList(orderItem));
        return dto;
    }


    private List<AfterSaleItemDO> buildAfterSaleItems(List<OrderItemDO> refundOrderItems, String afterSaleId) {
        List<AfterSaleItemDO> afterSaleItems = new ArrayList<>(refundOrderItems.size());
        for (OrderItemDO orderItem : refundOrderItems) {
            AfterSaleItemDO afterSaleItem = new AfterSaleItemDO();
            afterSaleItem.setAfterSaleId(afterSaleId);
            afterSaleItem.setOrderId(orderItem.getOrderId());
            afterSaleItem.setSkuCode(orderItem.getSkuCode());
            afterSaleItem.setProductName(orderItem.getProductName());
            afterSaleItem.setProductImg(orderItem.getProductImg());
            afterSaleItem.setReturnQuantity(orderItem.getSaleQuantity());
            afterSaleItem.setOriginAmount(orderItem.getOriginAmount());
            afterSaleItem.setApplyRefundAmount(orderItem.getOriginAmount());
            afterSaleItem.setRealRefundAmount(orderItem.getPayAmount());

            afterSaleItems.add(afterSaleItem);
        }
        return afterSaleItems;
    }

    private AfterSaleRefundDO buildAfterSaleRefund(AfterSaleInfoDO afterSaleInfo) {
        String orderId = afterSaleInfo.getOrderId();
        String afterSaleId = afterSaleInfo.getAfterSaleId();

        AfterSaleRefundDO afterSaleRefund = new AfterSaleRefundDO();
        afterSaleRefund.setAfterSaleRefundId(this.generateAfterSaleRefundId(afterSaleId));
        afterSaleRefund.setAfterSaleId(afterSaleId);
        afterSaleRefund.setOrderId(orderId);
        afterSaleRefund.setAfterSaleBatchNo(orderId + RandomHelper.getString(10, RandomHelper.DIST_NUMBER));
        afterSaleRefund.setAccountType(AccountTypeEnum.THIRD.getCode());
        // afterSaleRefund.setPayType();
        afterSaleRefund.setRefundStatus(RefundStatusEnum.UN_REFUND.getCode());
        afterSaleRefund.setRefundAmount(afterSaleInfo.getRealRefundAmount());
        // 实际退款的时候再记录退款时间
        // afterSaleRefund.setRefundPayTime();
        // afterSaleRefund.setOutTradeNo();
        afterSaleRefund.setRemark(RefundStatusEnum.UN_REFUND.getName());

        // 原路退回退款金额
        OrderPaymentDetailDO paymentDetail = orderPaymentDetailMapper.selectOneByOrderId(orderId);
        if (paymentDetail != null) {
            afterSaleRefund.setOutTradeNo(paymentDetail.getOutTradeNo());
            afterSaleRefund.setPayType(paymentDetail.getPayType());
        }
        return afterSaleRefund;
    }

    private String generateAfterSaleRefundId(String afterSaleId) {
        return afterSaleId + "_refund";
    }

    @Data
    private static class ReturnGoodsAssembleDTO {
        private String orderId;
        private String userId;
        private String afterSaleId;
        private String afterSaleRefundId;

        private AfterSaleTypeEnum afterSaleTypeEnum;
        private BigDecimal returnGoodAmount;
        private BigDecimal applyRefundAmount;
        private boolean lastReturnGoods;

        private List<OrderItemDO> refundOrderItems;
    }
}
