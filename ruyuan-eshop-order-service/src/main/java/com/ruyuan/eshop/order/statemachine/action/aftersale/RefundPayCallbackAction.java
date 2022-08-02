package com.ruyuan.eshop.order.statemachine.action.aftersale;

import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.enums.AfterSaleStateMachineChangeEnum;
import com.ruyuan.eshop.common.enums.AfterSaleStatusChangeEnum;
import com.ruyuan.eshop.common.enums.AfterSaleStatusEnum;
import com.ruyuan.eshop.common.utils.ParamCheckUtil;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleLogDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleRefundDO;
import com.ruyuan.eshop.order.domain.request.RefundCallbackRequest;
import com.ruyuan.eshop.order.enums.RefundStatusEnum;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.statemachine.action.AfterSaleStateAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.Objects;

/**
 * 售后支付回调退款Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
@Slf4j
public class RefundPayCallbackAction extends AfterSaleStateAction<AfterSaleStateMachineDTO> {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public AfterSaleStateMachineChangeEnum event() {
        return AfterSaleStateMachineChangeEnum.REFUND_DEFAULT;
    }

    @Override
    protected AfterSaleStateMachineDTO onStateChangeInternal(AfterSaleStateMachineChangeEnum event,
                                                             AfterSaleStateMachineDTO afterSaleStateMachineDTO) {

        RefundCallbackRequest refundCallbackRequest = afterSaleStateMachineDTO.getRefundCallbackRequest();
        Integer refundStatus = refundCallbackRequest.getRefundStatus();
        String afterSaleId = refundCallbackRequest.getAfterSaleId();

        String key = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
        try {
            boolean lock = redisLock.tryLock(key);
            if (!lock) {
                throw new OrderBizException(OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_REPEAT);
            }

            //  1、入参校验
            checkRefundCallbackParam(refundCallbackRequest);

            //  2、支付回调更新售后信息
            updateAfterSaleData(refundCallbackRequest, event, refundStatus);

            //  非退款成功 流程结束
            if (!RefundStatusEnum.REFUND_SUCCESS.getCode().equals(refundStatus)) {
                return afterSaleStateMachineDTO;
            }

            //  3、发短信
            orderAfterSaleService.sendRefundMobileMessage(afterSaleId);

            //  4、发APP通知
            orderAfterSaleService.sendRefundAppMessage(afterSaleId);

        } catch (Exception e) {
            throw new OrderBizException(OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_FAILED);
        } finally {
            redisLock.unlock(key);
        }

        return afterSaleStateMachineDTO;
    }

    private void updateAfterSaleData(RefundCallbackRequest refundCallbackRequest, AfterSaleStateMachineChangeEnum event,
                                     Integer refundStatus) {
        //  @Transactional无法生效，需要用编程式事务
        transactionTemplate.execute(transactionStatus -> {
            String afterSaleId = refundCallbackRequest.getAfterSaleId();
            //  封装 afterSaleInfoDO
            AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoDAO.getOneByAfterSaleId(afterSaleId);

            //  封装 afterSaleRefundDO
            AfterSaleRefundDO afterSaleRefundDO = new AfterSaleRefundDO();
            afterSaleRefundDO.setAfterSaleId(refundCallbackRequest.getAfterSaleId());
            afterSaleRefundDO.setRefundPayTime(refundCallbackRequest.getRefundTime());
            afterSaleRefundDO.setRefundStatus(
                    RefundStatusEnum.REFUND_SUCCESS.getCode().equals(refundStatus)
                            ? RefundStatusEnum.REFUND_SUCCESS.getCode()
                            : RefundStatusEnum.REFUND_FAIL.getCode()
            );
            afterSaleRefundDO.setRemark(
                    RefundStatusEnum.REFUND_SUCCESS.getCode().equals(refundStatus)
                            ? RefundStatusEnum.REFUND_SUCCESS.getMsg()
                            : RefundStatusEnum.REFUND_FAIL.getMsg()
            );

            AfterSaleLogDO afterSaleLogDO = new AfterSaleLogDO();
            //  退款成功
            if (RefundStatusEnum.REFUND_SUCCESS.getCode().equals(refundStatus)) {
                afterSaleLogDO = afterSaleOperateLogFactory.get(afterSaleInfoDO,
                        Objects.requireNonNull(
                                AfterSaleStatusChangeEnum.getBy(AfterSaleStatusEnum.REFUNDING.getCode(),
                                        event.getToStatus().getCode())));
            }
            //  退款失败
            if (RefundStatusEnum.REFUND_FAIL.getCode().equals(refundStatus)) {
                afterSaleLogDO = afterSaleOperateLogFactory.get(afterSaleInfoDO,
                        Objects.requireNonNull(
                                AfterSaleStatusChangeEnum.getBy(AfterSaleStatusEnum.REFUNDING.getCode(),
                                        AfterSaleStatusEnum.FAILED.getCode())));
            }

            //  更新 订单售后表
            afterSaleInfoDAO.updateStatus(afterSaleId, AfterSaleStatusEnum.REFUNDING.getCode(),
                    RefundStatusEnum.REFUND_SUCCESS.getCode().equals(refundStatus)
                            ? event.getToStatus().getCode()
                            : AfterSaleStatusEnum.FAILED.getCode()
            );

            //  新增 售后单变更表
            afterSaleLogDO.setOrderId(afterSaleInfoDO.getOrderId());
            afterSaleLogDAO.save(afterSaleLogDO);

            //  更新 售后退款单表
            afterSaleRefundDAO.updateAfterSaleRefundStatus(afterSaleRefundDO);
            return true;
        });
    }

    private void checkRefundCallbackParam(RefundCallbackRequest payRefundCallbackRequest) {
        ParamCheckUtil.checkObjectNonNull(payRefundCallbackRequest);

        String orderId = payRefundCallbackRequest.getOrderId();
        ParamCheckUtil.checkStringNonEmpty(orderId, OrderErrorCodeEnum.CANCEL_ORDER_ID_IS_NULL);

        String batchNo = payRefundCallbackRequest.getBatchNo();
        ParamCheckUtil.checkStringNonEmpty(batchNo, OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_BATCH_NO_IS_NULL);

        Integer refundStatus = payRefundCallbackRequest.getRefundStatus();
        ParamCheckUtil.checkObjectNonNull(refundStatus, OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_STATUS_NO_IS_NUL);

        Integer refundFee = payRefundCallbackRequest.getRefundFee();
        ParamCheckUtil.checkObjectNonNull(refundFee, OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_FEE_NO_IS_NUL);

        Integer totalFee = payRefundCallbackRequest.getTotalFee();
        ParamCheckUtil.checkObjectNonNull(totalFee, OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_TOTAL_FEE_NO_IS_NUL);

        String sign = payRefundCallbackRequest.getSign();
        ParamCheckUtil.checkStringNonEmpty(sign, OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_SIGN_NO_IS_NUL);

        String tradeNo = payRefundCallbackRequest.getTradeNo();
        ParamCheckUtil.checkStringNonEmpty(tradeNo, OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_TRADE_NO_IS_NUL);

        String afterSaleId = payRefundCallbackRequest.getAfterSaleId();
        ParamCheckUtil.checkStringNonEmpty(afterSaleId, OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_AFTER_SALE_ID_IS_NULL);

        Date refundTime = payRefundCallbackRequest.getRefundTime();
        ParamCheckUtil.checkObjectNonNull(refundTime, OrderErrorCodeEnum.PROCESS_PAY_REFUND_CALLBACK_AFTER_SALE_REFUND_TIME_IS_NULL);

        //  数据库中当前售后单不是未退款状态,表示已经退款成功 or 失败,那么本次就不能再执行支付回调退款
        AfterSaleRefundDO afterSaleByDatabase = afterSaleRefundDAO.findAfterSaleRefundByfterSaleId(afterSaleId);
        if (!RefundStatusEnum.UN_REFUND.getCode().equals(afterSaleByDatabase.getRefundStatus())) {
            throw new OrderBizException(OrderErrorCodeEnum.REPEAT_CALLBACK);
        }
    }
}
