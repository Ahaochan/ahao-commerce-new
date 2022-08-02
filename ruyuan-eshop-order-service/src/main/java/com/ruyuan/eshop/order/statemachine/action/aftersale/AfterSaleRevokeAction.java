package com.ruyuan.eshop.order.statemachine.action.aftersale;

import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.enums.*;
import com.ruyuan.eshop.common.utils.ParamCheckUtil;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleLogDO;
import com.ruyuan.eshop.order.domain.request.RevokeAfterSaleRequest;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.statemachine.action.AfterSaleStateAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * 撤销售后Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
@Slf4j
public class AfterSaleRevokeAction extends AfterSaleStateAction<AfterSaleStateMachineDTO> {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public AfterSaleStateMachineChangeEnum event() {
        return AfterSaleStateMachineChangeEnum.REVOKE_AFTER_SALE;
    }

    @Override
    protected AfterSaleStateMachineDTO onStateChangeInternal(AfterSaleStateMachineChangeEnum event, AfterSaleStateMachineDTO afterSaleStateMachineDTO) {
        return transactionTemplate.execute(transactionStatus -> {

            //  1、参数检查
            RevokeAfterSaleRequest revokeAfterSaleRequest = afterSaleStateMachineDTO.getRevokeAfterSaleRequest();
            AfterSaleInfoDO afterSaleInfoDO = checkRevokeParam(revokeAfterSaleRequest);

            //  2、加锁，锁整个售后单
            //  作用1:防并发、作用2:只要涉及售后表的更新，就需要加锁，锁整个售后表，否则算钱的时候，就会由于突然撤销，导致钱多算了
            String afterSaleId = revokeAfterSaleRequest.getAfterSaleId();
            String lockKey = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
            if (!redisLock.tryLock(lockKey)) {
                throw new OrderBizException(OrderErrorCodeEnum.AFTER_SALE_CANNOT_REVOKE);
            }

            try {
                //  3、更新售后单状态为："已撤销"
                updateAfterSaleInfo(afterSaleInfoDO);

                //  4、增加售后单操作日志
                insertAfterSaleLog(afterSaleInfoDO);

                //  5、更新售后条目的退货完成标记
                updateAfterSaleItemReturnCompletionMark(afterSaleId);

                //  6、删除售后订单条目和售后支付记录
                deleteAfterSaleData(afterSaleId);

            } finally {
                // 4、释放锁
                redisLock.unlock(lockKey);
            }
            return afterSaleStateMachineDTO;
        });
    }

    private void deleteAfterSaleData(String afterSaleId) {
        afterSaleItemDAO.delete(afterSaleId);
        afterSaleRefundDAO.delete(afterSaleId);
    }

    private void updateAfterSaleItemReturnCompletionMark(String afterSaleId) {
        /**
         * 售后条目数据格式: 售后条目不同(有优惠券条目、也有运费条目),但是after_sale_id、order_id、sku_code相同,所以可以get(0)
         * after_sale_id        order_id                sku_code    product_name
         * 2022011254135352100, 1022011270929057100,    10101011,   demo商品
         * 2022011254135352100, 1022011270929057100,    10101011,   1001001
         * 2022011254135352100, 1022011270929057100,    10101011,   运费
         */
        List<AfterSaleItemDO> afterSaleItemDOList = afterSaleItemDAO.listByAfterSaleId(afterSaleId);
        AfterSaleItemDO afterSaleItemDO = afterSaleItemDOList.get(0);
        String orderId = afterSaleItemDO.getOrderId();
        String skuCode = afterSaleItemDO.getSkuCode();

        //  如果是20，说明此次撤销的是售后条目的最后一笔,要把return_completion_mark由 "20已全部售后" 更新回 "10未全部售后"
        if (AfterSaleReturnCompletionMarkEnum.ALL_RETURN_GOODS.getCode().equals(afterSaleItemDO.getReturnCompletionMark())) {
            afterSaleItemDAO.rollbackAfterSaleItemCompletionMark(orderId,
                    AfterSaleReturnCompletionMarkEnum.NOT_ALL_RETURN_GOODS.getCode(), skuCode);
        }
    }

    private void insertAfterSaleLog(AfterSaleInfoDO afterSaleInfoDO) {
        AfterSaleLogDO afterSaleLogDO = afterSaleOperateLogFactory.get(afterSaleInfoDO, AfterSaleStatusChangeEnum.AFTER_SALE_REVOKED);
        afterSaleLogDO.setOrderId(afterSaleInfoDO.getOrderId());
        afterSaleLogDAO.save(afterSaleLogDO);
    }

    private void updateAfterSaleInfo(AfterSaleInfoDO afterSaleInfoDO) {
        afterSaleInfoDAO.updateStatus(afterSaleInfoDO.getAfterSaleId(), AfterSaleStatusEnum.COMMITTED.getCode(),
                AfterSaleStatusEnum.REVOKE.getCode());
    }

    private AfterSaleInfoDO checkRevokeParam(RevokeAfterSaleRequest revokeAfterSaleRequest) {
        ParamCheckUtil.checkObjectNonNull(revokeAfterSaleRequest, OrderErrorCodeEnum.REVOKE_AFTER_SALE_REQUEST_IS_NULL);
        ParamCheckUtil.checkObjectNonNull(revokeAfterSaleRequest.getAfterSaleId(), OrderErrorCodeEnum.AFTER_SALE_ID_IS_NULL);
        //  查询售后单
        String afterSaleId = revokeAfterSaleRequest.getAfterSaleId();
        AfterSaleInfoDO afterSaleInfo = afterSaleInfoDAO.getOneByAfterSaleId(afterSaleId);
        ParamCheckUtil.checkObjectNonNull(afterSaleInfo, OrderErrorCodeEnum.AFTER_SALE_ID_IS_NULL);

        //  校验售后单是否可以撤销：只有提交申请状态才可以撤销
        if (!AfterSaleStatusEnum.COMMITTED.getCode().equals(afterSaleInfo.getAfterSaleStatus())) {
            throw new OrderBizException(OrderErrorCodeEnum.AFTER_SALE_CANNOT_REVOKE);
        }

        //  业务限制:当前订单已发起尾笔条目的售后,不能再次撤回非尾笔的售后
        String orderId = afterSaleInfo.getOrderId();
        //  查询该笔订单的优惠券售后单和运费售后单
        List<AfterSaleItemDO> afterSaleItemCouponAndFreightList = afterSaleItemDAO.listAfterSaleCouponAndFreight(orderId);
        if (afterSaleItemCouponAndFreightList.isEmpty()) {
            //  该笔订单还没有优惠券售后单or运费售后单,允许撤销
            return afterSaleInfo;
        }
        //  发起撤销申请的售后id和该笔订单的尾笔售后id相同
        if (revokeAfterSaleRequest.getAfterSaleId().equals(afterSaleItemCouponAndFreightList.get(0).getAfterSaleId())) {
            //  尾笔条目自己撤销自己,允许撤销
            return afterSaleInfo;
        } else {
            //  在已有 尾笔优惠券售后单 or 运费售后单 的前提下, 已申请售后的非尾笔条目不允许撤销
            throw new OrderBizException(OrderErrorCodeEnum.CANNOT_REVOKE_AFTER_SALE);
        }

    }
}
