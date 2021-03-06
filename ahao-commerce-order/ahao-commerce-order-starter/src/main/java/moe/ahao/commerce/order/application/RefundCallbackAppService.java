package moe.ahao.commerce.order.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.RefundOrderCallbackCommand;
import moe.ahao.commerce.aftersale.infrastructure.component.AfterSaleOperateLogFactory;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusChangeEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.RefundStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleLogDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleRefundDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleInfoMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleLogMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleRefundMapper;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.exception.CommonBizExceptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
public class RefundCallbackAppService {
    @Autowired
    private AfterSaleInfoMapper afterSaleInfoMapper;
    @Autowired
    private AfterSaleLogMapper afterSaleLogMapper;
    @Autowired
    private AfterSaleRefundMapper afterSaleRefundMapper;

    @Autowired
    private AfterSaleOperateLogFactory afterSaleOperateLogFactory;

    @Autowired
    private RedissonClient redissonClient;

    public void refundCallback(RefundOrderCallbackCommand command) {
        String orderId = command.getOrderId();
        log.info("???????????????????????????, orderId:{}", orderId);
        // 1. ????????????
        this.check(command);

        // 2. ???????????????
        String afterSaleId = command.getAfterSaleId();
        String lockKey = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_REPEAT.msg();
        }

        try {
            // 3. ????????????????????????
            this.doRefundCallback(command);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_FAILED.msg();
        } finally {
            lock.unlock();
        }
    }

    /**
     * ?????????????????? ??????
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean doRefundCallback(RefundOrderCallbackCommand command) {
        String orderId = command.getOrderId();
        String afterSaleId = command.getAfterSaleId();

        // 1. ????????????????????????????????????????????????, ???????????????????????? or ??????, ????????????????????????????????????????????????
        AfterSaleRefundDO afterSaleByDatabase = afterSaleRefundMapper.selectOneByAfterSaleId(afterSaleId);
        if (!Objects.equals(RefundStatusEnum.UN_REFUND.getCode(), afterSaleByDatabase.getRefundStatus())) {
            throw OrderExceptionEnum.REPEAT_CALLBACK.msg();
        }

        // 2. ?????????????????????????????????????????????????????????
        this.updatePaymentRefundCallbackAfterSale(command);

        // 3. ?????????
        this.sendRefundMobileMessage(orderId);

        // 4. ???APP??????
        this.sendRefundAppMessage(orderId);
        return true;
    }

    private void check(RefundOrderCallbackCommand command) {
        if (command == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw OrderExceptionEnum.CANCEL_ORDER_ID_IS_NULL.msg();
        }

        String batchNo = command.getBatchNo();
        if (StringUtils.isEmpty(batchNo)) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_BATCH_NO_IS_NULL.msg();
        }

        Integer refundStatus = command.getRefundStatus();
        if (refundStatus == null) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_STATUS_NO_IS_NULL.msg();
        }

        BigDecimal refundFee = command.getRefundFee();
        if (refundFee == null) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_FEE_NO_IS_NULL.msg();
        }

        BigDecimal totalFee = command.getTotalFee();
        if (totalFee == null) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_TOTAL_FEE_NO_IS_NULL.msg();
        }

        String sign = command.getSign();
        if (StringUtils.isEmpty(sign)) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_SIGN_NO_IS_NULL.msg();
        }

        String tradeNo = command.getTradeNo();
        if (StringUtils.isEmpty(tradeNo)) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_TRADE_NO_IS_NULL.msg();
        }

        String afterSaleId = command.getAfterSaleId();
        if (StringUtils.isEmpty(afterSaleId)) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_AFTER_SALE_ID_IS_NULL.msg();
        }

        Date refundTime = command.getRefundTime();
        if (refundTime == null) {
            throw OrderExceptionEnum.PROCESS_PAY_REFUND_CALLBACK_AFTER_SALE_REFUND_TIME_IS_NULL.msg();
        }
    }

    /**
     * ????????????????????????????????????
     */
    public void updatePaymentRefundCallbackAfterSale(RefundOrderCallbackCommand command) {
        String afterSaleId = command.getAfterSaleId();
        AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoMapper.selectOneByAfterSaleId(afterSaleId);

        // 1. ??????????????????, ?????????????????????
        int toStatus;
        int refundStatus;
        String refundStatusMsg;
        if (Objects.equals(RefundStatusEnum.REFUND_SUCCESS.getCode(), command.getRefundStatus())) {
            toStatus = AfterSaleStatusEnum.REFUNDED.getCode();
            refundStatus = RefundStatusEnum.REFUND_SUCCESS.getCode();
            refundStatusMsg = RefundStatusEnum.REFUND_SUCCESS.getName();
        } else {
            toStatus = AfterSaleStatusEnum.FAILED.getCode();
            refundStatus = RefundStatusEnum.REFUND_FAIL.getCode();
            refundStatusMsg = RefundStatusEnum.REFUND_FAIL.getName();
        }

        // 1. ?????? ???????????????
        int fromStatus = AfterSaleStatusEnum.REFUNDING.getCode();
        afterSaleInfoMapper.updateAfterSaleStatusByAfterSaleId(afterSaleId, fromStatus, toStatus);

        // 2. ?????? ??????????????????
        AfterSaleLogDO afterSaleLogDO = afterSaleOperateLogFactory.get(afterSaleInfoDO, AfterSaleStatusChangeEnum.getBy(fromStatus, toStatus));
        afterSaleLogMapper.insert(afterSaleLogDO);

        // 3. ?????? ??????????????????
        afterSaleRefundMapper.updateRefundInfoByAfterSaleId(afterSaleId, refundStatus, command.getRefundTime(), refundStatusMsg);
    }

    /**
     * ??????????????????
     */
    public void sendRefundMobileMessage(String orderId) {
        log.info("?????????????????????, ?????????:{}", orderId);
    }

    /**
     * ??????????????????
     */
    public void sendRefundAppMessage(String orderId) {
        log.info("???????????????APP??????, ?????????:{}", orderId);
    }
}
