package com.ruyuan.eshop.order.service.impl;

import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.enums.*;
import com.ruyuan.eshop.common.message.RefundMessage;
import com.ruyuan.eshop.common.redis.RedisLock;
import com.ruyuan.eshop.order.dao.*;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.domain.request.*;
import com.ruyuan.eshop.order.enums.RefundStatusEnum;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.service.OrderAfterSaleService;
import com.ruyuan.eshop.order.statemachine.StateMachineFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class OrderAfterSaleServiceImpl implements OrderAfterSaleService {

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private OrderInfoDAO orderInfoDAO;

    @Autowired
    private OrderCancelScheduledTaskDAO orderCancelScheduledTaskDAO;

    @Autowired
    private StateMachineFactory stateMachineFactory;

    @Override
    public JsonResult<Boolean> cancelOrder(CancelOrderRequest cancelOrderRequest) {
        //  售后状态机 操作 取消订单 OrderCancelAction
        StateMachineFactory.OrderStateMachine orderStateMachine = stateMachineFactory.getOrderStateMachine(OrderStatusEnum.CANCELLED);
        orderStateMachine.fire(OrderStatusChangeEnum.ORDER_CANCEL, cancelOrderRequest);
        return JsonResult.buildSuccess(true);
    }

    @Override
    public JsonResult<Boolean> processCancelOrder(CancelOrderAssembleRequest cancelOrderAssembleRequest) {

        AfterSaleStateMachineDTO afterSaleStateMachineDTO = new AfterSaleStateMachineDTO();
        afterSaleStateMachineDTO.setCancelOrderAssembleRequest(cancelOrderAssembleRequest);

        //  售后状态机 操作 取消订单时记录售后信息 CancelOrderCreatedInfoAction
        StateMachineFactory.AfterSaleStateMachine afterSaleStateMachine = stateMachineFactory.getAfterSaleStateMachine(AfterSaleStatusEnum.UN_CREATED);
        afterSaleStateMachine.fire(AfterSaleStateMachineChangeEnum.CANCEL_ORDER, afterSaleStateMachineDTO);

        return JsonResult.buildSuccess(true);
    }


    @Override
    public JsonResult<Boolean> sendRefundMobileMessage(String orderId) {
        log.info("发退款通知短信,订单号:{}", orderId);
        return JsonResult.buildSuccess();
    }

    @Override
    public JsonResult<Boolean> sendRefundAppMessage(String orderId) {
        log.info("发退款通知APP信息,订单号:{}", orderId);
        return JsonResult.buildSuccess();
    }

    @Override
    public JsonResult<Boolean> refundMoney(RefundMessage refundMessage) {

        AfterSaleStateMachineDTO afterSaleStateMachineDTO = new AfterSaleStateMachineDTO();
        afterSaleStateMachineDTO.setRefundMessage(refundMessage);

        //  售后状态机 操作 实际退款中通过更新售后信息 RefundingAction
        StateMachineFactory.AfterSaleStateMachine afterSaleStateMachine = stateMachineFactory.getAfterSaleStateMachine(AfterSaleStatusEnum.REVIEW_PASS);
        afterSaleStateMachine.fire(AfterSaleStateMachineChangeEnum.REFUNDING, afterSaleStateMachineDTO);

        return JsonResult.buildSuccess(true);
    }

    /**
     * 业务更新说明：更新售后业务,将尾笔订单条目的验证标准细化到sku数量维度
     * <p>
     * 售后单生成规则说明：<br>
     * 1.当前售后条目非尾笔,只生成一笔订单条目的售后单<br>
     * 2.当前售后条目尾笔,生成3笔售后单,分别是:订单条目售后单、优惠券售后单、运费售后单
     * <p>
     * 业务场景说明：<br>
     * 场景1：订单有2笔条目,A条目商品总数量:10,B条目商品总数量:1<br>
     * 第一次：A发起售后,售后数量1,已退数量1<br>
     * 第二次：A发起售后,售后数量2,已退数量3<br>
     * 第三次：A发起售后,售后数量7,已退数量10,A条目全部退完<br>
     * 第四次：B发起售后,售后数量1,已退数量1,本次售后条目是当前订单的最后一条,补退优惠券和运费<br>
     * <p>
     * 场景2：订单有1笔条目,条目商品总数量和申请售后数量相同,直接全部退掉,补退优惠券和运费
     * <p>
     * 场景3：订单有1笔条目,条目商品总数量2<br>
     * 第一次：条目申请售后,售后数量1<br>
     * 第二次：条目申请售后,售后数量1,本次售后条目是当前订单的最后一条,补退优惠券和运费
     */
    @Override
    public JsonResult<Boolean> processApplyAfterSale(ReturnGoodsOrderRequest returnGoodsOrderRequest) {
        AfterSaleStateMachineDTO afterSaleStateMachineDTO = new AfterSaleStateMachineDTO();
        afterSaleStateMachineDTO.setReturnGoodsOrderRequest(returnGoodsOrderRequest);

        //  售后状态机 操作 售后数据落库 AfterSaleCreatedInfoAction
        StateMachineFactory.AfterSaleStateMachine afterSaleStateMachine = stateMachineFactory.getAfterSaleStateMachine(AfterSaleStatusEnum.UN_CREATED);
        afterSaleStateMachine.fire(AfterSaleStateMachineChangeEnum.INITIATE_AFTER_SALE, afterSaleStateMachineDTO);

        return JsonResult.buildSuccess(true);
    }

    @Override
    public JsonResult<Boolean> receivePaymentRefundCallback(RefundCallbackRequest refundCallbackRequest) {
        //  未退款
        if (RefundStatusEnum.UN_REFUND.getCode().equals(refundCallbackRequest.getRefundStatus())) {
            throw new OrderBizException(OrderErrorCodeEnum.PAY_REFUND_CALLBACK_STATUS_FAILED);
        }

        //  售后状态机 操作 支付回调退款成功 更新售后信息 RefundPayCallbackAction
        AfterSaleStateMachineDTO afterSaleStateMachineDTO = new AfterSaleStateMachineDTO();
        afterSaleStateMachineDTO.setRefundCallbackRequest(refundCallbackRequest);

        StateMachineFactory.AfterSaleStateMachine afterSaleStateMachine = stateMachineFactory.getAfterSaleStateMachine(AfterSaleStatusEnum.REFUNDING);
        afterSaleStateMachine.fire(AfterSaleStateMachineChangeEnum.REFUND_DEFAULT, afterSaleStateMachineDTO);

        return JsonResult.buildSuccess(true);
    }

    @Override
    public void revokeAfterSale(RevokeAfterSaleRequest revokeAfterSaleRequest) {
        //  售后状态机 操作 售后撤销 AfterSaleRevokeAction
        AfterSaleStateMachineDTO afterSaleStateMachineDTO = new AfterSaleStateMachineDTO();
        afterSaleStateMachineDTO.setRevokeAfterSaleRequest(revokeAfterSaleRequest);

        StateMachineFactory.AfterSaleStateMachine afterSaleStateMachine = stateMachineFactory.getAfterSaleStateMachine(AfterSaleStatusEnum.COMMITTED);
        afterSaleStateMachine.fire(AfterSaleStateMachineChangeEnum.REVOKE_AFTER_SALE, afterSaleStateMachineDTO);
    }

    @Override
    public void verifyBeforeOrderCancellation(String orderId) {
        //  分布式锁(与预支付、订单支付回调加的是同一把锁)
        String key = RedisLockKeyConstants.ORDER_PAY_KEY + orderId;
        boolean lock = redisLock.tryLock(key);
        if (!lock) {
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_CANNOT_OPERATED_IN_MULTIPLE_PLACES);
        }
        try {
            //  查询订单实时状态
            OrderInfoDO orderInfoDO = orderInfoDAO.getByOrderId(orderId);

            //  参数和幂等校验，如果校验失败 本次流程直接结束
            if (!checkParamIdempotent(orderInfoDO)) {
                return;
            }

            //  组装调用执行取消订单接口参数
            CancelOrderRequest cancelOrderRequest = buildCancelOrderRequest(orderInfoDO);

            //  执行取消订单
            cancelOrder(cancelOrderRequest);

            //  删除任务记录
            orderCancelScheduledTaskDAO.remove(orderInfoDO.getOrderId());
        } finally {
            // 释放分布式锁
            redisLock.unlock(key);
        }
    }

    private Boolean checkParamIdempotent(OrderInfoDO orderInfoDO) {
        //  参数校验
        if (orderInfoDO == null) {
            return false;
        }

        //  幂等校验 当前订单的任务记录不存在 结束流程
        OrderCancelScheduledTaskDO orderCancelScheduledTaskDO = orderCancelScheduledTaskDAO.getByOrderId(orderInfoDO.getOrderId());
        if (orderCancelScheduledTaskDO == null) {
            return false;
        }

        //  订单非"已创建"状态,说明有了其他的操作,可以删除掉兜底记录  结束流程
        if (!OrderStatusEnum.CREATED.getCode().equals(orderInfoDO.getOrderStatus())) {
            // 删除任务记录
            orderCancelScheduledTaskDAO.remove(orderInfoDO.getOrderId());
            return false;
        }

        //  如果  当前时间 > 订单实际支付截止时间 返回true, 否则false
        return System.currentTimeMillis() > orderInfoDO.getExpireTime().getTime();
    }

    private CancelOrderRequest buildCancelOrderRequest(OrderInfoDO orderInfoDO) {
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderInfoDO.getOrderId());
        cancelOrderRequest.setBusinessIdentifier(orderInfoDO.getBusinessIdentifier());
        cancelOrderRequest.setCancelType(orderInfoDO.getOrderType());
        cancelOrderRequest.setUserId(orderInfoDO.getUserId());
        cancelOrderRequest.setOrderType(orderInfoDO.getOrderType());
        cancelOrderRequest.setOrderStatus(orderInfoDO.getOrderStatus());

        return cancelOrderRequest;
    }
}
