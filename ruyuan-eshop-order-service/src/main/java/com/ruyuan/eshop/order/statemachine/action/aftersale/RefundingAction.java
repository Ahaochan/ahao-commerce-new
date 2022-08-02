package com.ruyuan.eshop.order.statemachine.action.aftersale;

import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.enums.*;
import com.ruyuan.eshop.common.message.RefundMessage;
import com.ruyuan.eshop.market.domain.request.ReleaseUserCouponRequest;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.service.RocketMqService;
import com.ruyuan.eshop.order.statemachine.action.AfterSaleStateAction;
import com.ruyuan.eshop.pay.domain.request.PayRefundRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
 * 实际退款Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
@Slf4j
public class RefundingAction extends AfterSaleStateAction<AfterSaleStateMachineDTO> {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RocketMqService rocketMqService;

    @Override
    public AfterSaleStateMachineChangeEnum event() {
        return AfterSaleStateMachineChangeEnum.REFUNDING;
    }

    @Override
    protected AfterSaleStateMachineDTO onStateChangeInternal(AfterSaleStateMachineChangeEnum event,
                                                             AfterSaleStateMachineDTO afterSaleStateMachineDTO) {
        //  @Transactional无法生效，需要用编程式事务
        return transactionTemplate.execute(transactionStatus -> {
            RefundMessage refundMessage = afterSaleStateMachineDTO.getRefundMessage();
            //  分布式锁
            String afterSaleId = refundMessage.getAfterSaleId();
            String key = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
            boolean lock = redisLock.tryLock(key);
            if (!lock) {
                throw new OrderBizException(OrderErrorCodeEnum.REFUND_MONEY_REPEAT);
            }

            //  退款操作
            try {
                AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoDAO.getOneByAfterSaleId(refundMessage.getAfterSaleId());
                AfterSaleRefundDO afterSaleRefundDO = afterSaleRefundDAO.findAfterSaleRefundByfterSaleId(String.valueOf(afterSaleId));

                //  1、封装调用支付退款接口的数据
                PayRefundRequest payRefundRequest = buildPayRefundRequest(refundMessage, afterSaleRefundDO);

                //  2、执行退款
                payRemote.executeRefund(payRefundRequest);

                //  3、取消订单流程 更新售后单状态后流程结束
                if (AfterSaleTypeEnum.RETURN_MONEY.getCode().equals(refundMessage.getAfterSaleType())) {
                    updateAfterSaleStatus(afterSaleInfoDO, event);
                    return afterSaleStateMachineDTO;
                }

                //  4、手动售后流程
                //  查询售后优惠券条目
                AfterSaleItemDO afterSaleItemDO = afterSaleItemDAO.getAfterSaleOrderItem(refundMessage.getOrderId(),
                        afterSaleId, refundMessage.getSkuCode(), AfterSaleItemTypeEnum.AFTER_SALE_COUPON.getCode());
                //  没有使用优惠券,说明这笔售后不是尾笔 更新售后单状态后流程结束
                if (afterSaleItemDO == null) {
                    updateAfterSaleStatus(afterSaleInfoDO, event);
                    return afterSaleStateMachineDTO;
                }

                //  5、更新售后单状态
                updateAfterSaleStatus(afterSaleInfoDO, event);

                //  6、发送释放优惠券mq
                sendReleaseCouponMq(afterSaleId, refundMessage);

                return afterSaleStateMachineDTO;

            } finally {
                redisLock.unlock(key);
            }
        });
    }

    private PayRefundRequest buildPayRefundRequest(RefundMessage refundMessage, AfterSaleRefundDO afterSaleRefundDO) {
        String orderId = refundMessage.getOrderId();
        PayRefundRequest payRefundRequest = new PayRefundRequest();
        payRefundRequest.setOrderId(orderId);
        payRefundRequest.setAfterSaleId(refundMessage.getAfterSaleId());
        payRefundRequest.setRefundAmount(afterSaleRefundDO.getRefundAmount());

        return payRefundRequest;
    }

    /**
     * 更新售后单状态
     */
    public void updateAfterSaleStatus(AfterSaleInfoDO afterSaleInfoDO, AfterSaleStateMachineChangeEnum event) {
        String afterSaleId = afterSaleInfoDO.getAfterSaleId();
        Integer fromStatus = event.getFromStatus().getCode();
        Integer toStatus = event.getToStatus().getCode();
        AfterSaleStatusChangeEnum changeEnum = AfterSaleStatusChangeEnum.getBy(fromStatus, toStatus);
        if (changeEnum == null) {
            throw new OrderBizException(OrderErrorCodeEnum.ENUM_STATUS_IS_NULL);
        }
        AfterSaleLogDO afterSaleLogDO = afterSaleOperateLogFactory.get(afterSaleInfoDO, changeEnum);

        //  更新 订单售后表
        afterSaleInfoDAO.updateStatus(afterSaleId, fromStatus, toStatus);
        //  新增 售后单变更表
        afterSaleLogDO.setOrderId(afterSaleInfoDO.getOrderId());
        afterSaleLogDAO.save(afterSaleLogDO);

        log.info("保存售后变更记录,售后单号:{},fromStatus:{}, toStatus:{}", afterSaleId, fromStatus, toStatus);
    }

    private ReleaseUserCouponRequest buildLastOrderReleasesCouponMessage(String afterSaleId, RefundMessage refundMessage) {
        //  组装释放优惠券权益消息数据
        String orderId = refundMessage.getOrderId();
        OrderInfoDO orderInfoDO = orderInfoDAO.getByOrderId(orderId);
        ReleaseUserCouponRequest releaseUserCouponRequest = new ReleaseUserCouponRequest();
        releaseUserCouponRequest.setCouponId(orderInfoDO.getCouponId());
        releaseUserCouponRequest.setUserId(orderInfoDO.getUserId());
        releaseUserCouponRequest.setAfterSaleId(afterSaleId);

        return releaseUserCouponRequest;
    }

    private void sendReleaseCouponMq(String afterSaleId, RefundMessage refundMessage) {
        ReleaseUserCouponRequest releaseUserCouponRequest = buildLastOrderReleasesCouponMessage(afterSaleId, refundMessage);
        rocketMqService.sendAfterSaleReleaseCouponMessage(releaseUserCouponRequest);
    }
}
