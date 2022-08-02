package com.ruyuan.eshop.order.service.impl;

import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.enums.DeleteStatusEnum;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import com.ruyuan.eshop.common.enums.PayTypeEnum;
import com.ruyuan.eshop.common.redis.RedisLock;
import com.ruyuan.eshop.common.utils.LoggerFormat;
import com.ruyuan.eshop.common.utils.ParamCheckUtil;
import com.ruyuan.eshop.order.converter.OrderConverter;
import com.ruyuan.eshop.order.dao.OrderDeliveryDetailDAO;
import com.ruyuan.eshop.order.dao.OrderInfoDAO;
import com.ruyuan.eshop.order.dao.OrderPaymentDetailDAO;
import com.ruyuan.eshop.order.domain.dto.CreateOrderDTO;
import com.ruyuan.eshop.order.domain.dto.GenOrderIdDTO;
import com.ruyuan.eshop.order.domain.dto.PrePayOrderDTO;
import com.ruyuan.eshop.order.domain.entity.OrderDeliveryDetailDO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.domain.entity.OrderPaymentDetailDO;
import com.ruyuan.eshop.order.domain.request.*;
import com.ruyuan.eshop.order.enums.OrderNoTypeEnum;
import com.ruyuan.eshop.order.enums.PayStatusEnum;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.manager.OrderNoManager;
import com.ruyuan.eshop.order.remote.PayRemote;
import com.ruyuan.eshop.order.service.OrderService;
import com.ruyuan.eshop.order.statemachine.StateMachineFactory;
import com.ruyuan.eshop.pay.domain.dto.PayOrderDTO;
import com.ruyuan.eshop.pay.domain.request.PayOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoDAO orderInfoDAO;

    @Autowired
    private OrderDeliveryDetailDAO orderDeliveryDetailDAO;

    @Autowired
    private OrderPaymentDetailDAO orderPaymentDetailDAO;

    @Autowired
    private OrderNoManager orderNoManager;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private PayRemote payRemote;

    @Autowired
    private OrderConverter orderConverter;

    // 状态机工厂，是组件，专门是用来创建状态机的
    @Autowired
    private StateMachineFactory stateMachineFactory;

    /**
     * 生成订单号接口
     *
     * @param genOrderIdRequest 生成订单号入参
     * @return 订单号
     */
    @Override
    public GenOrderIdDTO genOrderId(GenOrderIdRequest genOrderIdRequest) {
        log.info(LoggerFormat.build()
                .remark("genOrderId->request")
                .data("request", genOrderIdRequest)
                .finish());

        // 参数检查
        String userId = genOrderIdRequest.getUserId();
        ParamCheckUtil.checkStringNonEmpty(userId);
        Integer businessIdentifier = genOrderIdRequest.getBusinessIdentifier();
        ParamCheckUtil.checkObjectNonNull(businessIdentifier);

        String orderId = orderNoManager.genOrderId(OrderNoTypeEnum.SALE_ORDER.getCode(), userId);
        GenOrderIdDTO genOrderIdDTO = new GenOrderIdDTO();
        genOrderIdDTO.setOrderId(orderId);

        log.info(LoggerFormat.build()
                .remark("genOrderId->response")
                .data("response", genOrderIdDTO)
                .finish());
        return genOrderIdDTO;
    }

    /**
     * 提交订单/生成订单接口
     *
     * @param createOrderRequest 提交订单请求入参
     * @return 订单号
     */
    @Override
    public CreateOrderDTO createOrder(CreateOrderRequest createOrderRequest) {
        log.info(LoggerFormat.build()
                .remark("createOrder->request")
                .data("request", createOrderRequest)
                .finish());

        // 状态机流转
        // 分析一下这块的代码，StateMachineFactory，状态机工厂，工厂设计模式，来创建出来订单状态机
        // 刚开始初始化订单状态机的时候，订单初始的状态state，是null
        // 通过状态机fire触发了一个状态流转，把订单状态从null，流转到了created状态，基于状态机触发了状态的流转，把request对象作为数据传递
        // 状态机进行了状态流转的时候，必然会触发说你的状态流转到created以后，要执行一个action动作，每个state -> action，猜测一下
        // 如果执行了created action，此时就必然会导致生单业务流程编排和触发
        // 最终一致性框架，业务流程编排框架，我们之前用了2周时间，已经分析的极为的透彻了
        // 研究一下，状态机到底是如何来实现的，状态机我们没有自研，而是用的是开源的框架，

        // 基于squirrel框架，直接就可以拿到订单状态机，发生什么事件，会导致状态如何流转，调用他的什么方法
        // 全部都定义好了
        StateMachineFactory.OrderStateMachine orderStateMachine = stateMachineFactory.getOrderStateMachine(OrderStatusEnum.NULL);
        // 我们还把创建订单request对象传递进来了，这个对象，是需要后面来进行使用的
        // 状态机，初始的状态是null，创建事件，触发了以后，会从null -> created，流转之后，会在这里触发状态机里的方法的执行
        orderStateMachine.fire(OrderStatusChangeEnum.ORDER_CREATED, createOrderRequest);

        // 基于状态机触发主单创建事件，触发null->created流转，触发created action，触发生单业务流程编排
        // 每次生单完成了，post里，把这个订单状态变更，推送到消息总线里去，mq把你的所有状态变更，都推送过去这样子
        // created action，post逻辑，判断一下是否要进行拆单，对每个子单跑一下子单生单业务流程编排

        // 返回订单信息
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setOrderId(createOrderRequest.getOrderId());
        return createOrderDTO;
    }


    /**
     * 预支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrePayOrderDTO prePayOrder(PrePayOrderRequest prePayOrderRequest) {
        log.info(LoggerFormat.build()
                .remark("prePayOrder->request")
                .data("request", prePayOrderRequest)
                .finish());

        // 提取业务参数
        String orderId = prePayOrderRequest.getOrderId();
        Integer payAmount = prePayOrderRequest.getPayAmount();

        // 入参检查
        checkPrePayOrderRequestParam(prePayOrderRequest, orderId, payAmount);

        // 加分布式锁（与订单支付回调时加的是同一把锁）
        // 分布式锁去确保支付并发问题
        String key = RedisLockKeyConstants.ORDER_PAY_KEY + orderId;
        prePayOrderLock(key);
        try {
            // 幂等性检查
            checkPrePayOrderInfo(orderId, payAmount);

            // 调用支付系统进行预支付
            // 直接在里调用支付系统的接口，对第三方支付平台，去发起支付请求和操作
            PayOrderRequest payOrderRequest = orderConverter.convertPayOrderRequest(prePayOrderRequest);
            PayOrderDTO payOrderDTO = payRemote.payOrder(payOrderRequest);

            // 状态机流转 -> 更新支付信息
            OrderStatusChangeEnum statusChangeEnum = OrderStatusChangeEnum.ORDER_PREPAY;
            // 预支付的时候，状态流转是created to created，状态是不会变化，拿到订单预支付的事件
            // 这个订单状态机，他的初始化的状态，就是created
            // 会根据你的事件名称，prepay事件，去获取到对应的action，来执行action逻辑
            StateMachineFactory.OrderStateMachine orderStateMachine = stateMachineFactory.getOrderStateMachine(statusChangeEnum.getFromStatus());
            orderStateMachine.fire(statusChangeEnum, payOrderDTO);

            // 返回结果
            PrePayOrderDTO prePayOrderDTO = orderConverter.convertPrePayOrderRequest(payOrderDTO);
            log.info(LoggerFormat.build()
                    .remark("prePayOrder->response")
                    .data("response", prePayOrderDTO)
                    .finish());
            return prePayOrderDTO;
        } finally {
            // 释放分布式锁
            redisLock.unlock(key);
        }
    }

    /**
     * 预支付加分布式锁
     */
    private void prePayOrderLock(String key) {
        boolean lock = redisLock.tryLock(key);
        if (!lock) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_PRE_PAY_ERROR);
        }
    }

    /**
     * 预支付订单的前置检查
     */
    private void checkPrePayOrderInfo(String orderId, Integer payAmount) {
        // 查询订单信息
        OrderInfoDO orderInfoDO = orderInfoDAO.getByOrderId(orderId);
        OrderPaymentDetailDO orderPaymentDetailDO = orderPaymentDetailDAO.getPaymentDetailByOrderId(orderId);
        if (orderInfoDO == null || orderPaymentDetailDO == null) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_INFO_IS_NULL);
        }

        // 检查订单支付金额
        if (!payAmount.equals(orderInfoDO.getPayAmount())) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_PAY_AMOUNT_ERROR);
        }

        // 判断一下订单状态
        if (!OrderStatusEnum.CREATED.getCode().equals(orderInfoDO.getOrderStatus())) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_STATUS_ERROR);
        }

        // 判断一下支付状态
        if (PayStatusEnum.PAID.getCode().equals(orderPaymentDetailDO.getPayStatus())) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_PAY_STATUS_IS_PAID);
        }

        // 判断是否超过了支付超时时间
        Date curDate = new Date();
        if (curDate.after(orderInfoDO.getExpireTime())) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_PRE_PAY_EXPIRE_ERROR);
        }
    }

    /**
     * 检查预支付接口入参
     */
    private void checkPrePayOrderRequestParam(PrePayOrderRequest prePayOrderRequest, String orderId, Integer payAmount) {
        String userId = prePayOrderRequest.getUserId();
        ParamCheckUtil.checkStringNonEmpty(userId, OrderErrorCodeEnum.USER_ID_IS_NULL);

        String businessIdentifier = prePayOrderRequest.getBusinessIdentifier();
        ParamCheckUtil.checkStringNonEmpty(businessIdentifier, OrderErrorCodeEnum.BUSINESS_IDENTIFIER_ERROR);

        Integer payType = prePayOrderRequest.getPayType();
        ParamCheckUtil.checkObjectNonNull(payType, OrderErrorCodeEnum.PAY_TYPE_PARAM_ERROR);
        if (PayTypeEnum.getByCode(payType) == null) {
            throw new OrderBizException(OrderErrorCodeEnum.PAY_TYPE_PARAM_ERROR);
        }

        ParamCheckUtil.checkStringNonEmpty(orderId, OrderErrorCodeEnum.ORDER_ID_IS_NULL);
        ParamCheckUtil.checkObjectNonNull(payAmount, OrderErrorCodeEnum.PAY_TYPE_PARAM_ERROR);
    }

    /**
     * 支付回调
     * 支付回调有2把分布式锁的原因说明：同一笔订单在同一时间只能支付or取消
     * 不可以同时对一笔订单，既发起支付，又发起取消
     */
    @Override
    public void payCallback(PayCallbackRequest payCallbackRequest) {
        log.info(LoggerFormat.build()
                .remark("payCallback->request")
                .data("request", payCallbackRequest)
                .finish());

        // 状态机流转
        OrderStatusChangeEnum event = OrderStatusChangeEnum.ORDER_PAID;
        StateMachineFactory.OrderStateMachine orderStateMachine = stateMachineFactory.getOrderStateMachine(event.getFromStatus());
        orderStateMachine.fire(event, payCallbackRequest);
    }


    @Override
    public void removeOrders(List<String> orderIds) {
        //1、根据id查询订单
        List<OrderInfoDO> orders = orderInfoDAO.listByOrderIds(orderIds);
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }

        //2、校验订单是否可以移除
        orders.forEach(order -> {
            if (!canRemove(order)) {
                throw new OrderBizException(OrderErrorCodeEnum.ORDER_CANNOT_REMOVE);
            }
        });

        //3、对订单进行软删除
        List<Long> ids = orders.stream().map(OrderInfoDO::getId).collect(Collectors.toList());
        orderInfoDAO.softRemoveOrders(ids);
    }

    private boolean canRemove(OrderInfoDO order) {
        return OrderStatusEnum.canRemoveStatus().contains(order.getOrderStatus()) &&
                DeleteStatusEnum.NO.getCode().equals(order.getDeleteStatus());
    }

    @Override
    public void adjustDeliveryAddress(AdjustDeliveryAddressRequest request) {
        //1、根据id查询订单
        OrderInfoDO order = orderInfoDAO.getByOrderId(request.getOrderId());
        ParamCheckUtil.checkObjectNonNull(order, OrderErrorCodeEnum.ORDER_NOT_FOUND);

        //2、校验订单是否未出库
        if (!OrderStatusEnum.unOutStockStatus().contains(order.getOrderStatus())) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_NOT_ALLOW_TO_ADJUST_ADDRESS);
        }

        //3、查询订单配送信息
        OrderDeliveryDetailDO orderDeliveryDetail = orderDeliveryDetailDAO.getByOrderId(request.getOrderId());
        if (null == orderDeliveryDetail) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_DELIVERY_NOT_FOUND);
        }

        //4、校验配送信息是否已经被修改过一次
        if (orderDeliveryDetail.getModifyAddressCount() > 0) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_DELIVERY_ADDRESS_HAS_BEEN_ADJUSTED);
        }

        //5、更新配送地址信息
        orderDeliveryDetailDAO.updateDeliveryAddress(orderDeliveryDetail.getId()
                , orderDeliveryDetail.getModifyAddressCount(), request);
    }
}