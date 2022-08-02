package com.ruyuan.eshop.order.statemachine.action.aftersale;

import com.google.common.collect.Lists;
import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.enums.*;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.dto.CancelOrderRefundAmountDTO;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.dto.OrderItemDTO;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.domain.request.CancelOrderAssembleRequest;
import com.ruyuan.eshop.order.enums.OrderCancelTypeEnum;
import com.ruyuan.eshop.order.enums.OrderNoTypeEnum;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.service.RocketMqService;
import com.ruyuan.eshop.order.service.impl.OrderPaymentDetailFactory;
import com.ruyuan.eshop.order.statemachine.action.AfterSaleStateAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 取消订单创建售后信息Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
@Slf4j
public class CancelOrderCreatedInfoAction extends AfterSaleStateAction<AfterSaleStateMachineDTO> {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RocketMqService rocketMqService;

    @Autowired
    private OrderPaymentDetailFactory orderPaymentDetailFactory;

    @Override
    public AfterSaleStateMachineChangeEnum event() {
        return AfterSaleStateMachineChangeEnum.CANCEL_ORDER;
    }

    @Override
    protected AfterSaleStateMachineDTO onStateChangeInternal(AfterSaleStateMachineChangeEnum event,
                                                             AfterSaleStateMachineDTO afterSaleStateMachineDTO) {
        //  @Transactional无法生效，需要用编程式事务
        return transactionTemplate.execute(transactionStatus -> {
            CancelOrderAssembleRequest cancelOrderAssembleRequest = afterSaleStateMachineDTO.getCancelOrderAssembleRequest();
            String orderId = cancelOrderAssembleRequest.getOrderId();

            //  分布式锁
            String key = RedisLockKeyConstants.REFUND_KEY + orderId;
            boolean lock = redisLock.tryLock(key);
            if (!lock) {
                throw new OrderBizException(OrderErrorCodeEnum.PROCESS_REFUND_REPEAT);
            }

            //  执行退款前的准备工作
            try {
                //  生成售后订单号
                OrderInfoDO orderInfoDO = orderConverter.orderInfoDTO2DO(cancelOrderAssembleRequest.getOrderInfoDTO());
                String afterSaleId = orderNoManager.genOrderId(OrderNoTypeEnum.AFTER_SALE.getCode(), orderInfoDO.getUserId());
                cancelOrderAssembleRequest.setAfterSaleId(afterSaleId);

                //  1、计算退款金额
                CancelOrderRefundAmountDTO cancelOrderRefundAmountDTO = calculatingCancelOrderRefundAmount(cancelOrderAssembleRequest);
                cancelOrderAssembleRequest.setCancelOrderRefundAmountDTO(cancelOrderRefundAmountDTO);

                //  2、新增售后信息
                insertAfterSaleInfo(afterSaleStateMachineDTO, event);

                //  3、发送实际退款MQ
                sendActualRefundMq(cancelOrderAssembleRequest);

            } finally {
                redisLock.unlock(key);
            }
            return afterSaleStateMachineDTO;
        });
    }

    private void sendActualRefundMq(CancelOrderAssembleRequest cancelOrderAssembleRequest) {

        rocketMqService.sendCancelOrderRefundMessage(cancelOrderAssembleRequest);
    }

    private void insertAfterSaleInfo(AfterSaleStateMachineDTO afterSaleStateMachineDTO, AfterSaleStateMachineChangeEnum event) {
        CancelOrderAssembleRequest cancelOrderAssembleRequest = afterSaleStateMachineDTO.getCancelOrderAssembleRequest();
        String orderId = cancelOrderAssembleRequest.getOrderId();
        OrderInfoDO orderInfoDO = orderInfoDAO.getByOrderId(orderId);
        String afterSaleId = cancelOrderAssembleRequest.getAfterSaleId();

        //  取消订单过程中的 申请退款金额 和 实际退款金额 都是实付退款金额 金额相同
        AfterSaleInfoDO afterSaleInfoDO = new AfterSaleInfoDO();
        afterSaleInfoDO.setApplyRefundAmount(orderInfoDO.getPayAmount());
        afterSaleInfoDO.setRealRefundAmount(orderInfoDO.getPayAmount());

        //  1、新增售后订单表
        insertCancelOrderAfterSaleInfoTable(orderInfoDO, event.getToStatus().getCode(), afterSaleInfoDO, afterSaleId);
        cancelOrderAssembleRequest.setAfterSaleId(afterSaleId);

        //  2、新增售后条目表
        List<OrderItemDTO> orderItemDTOList = cancelOrderAssembleRequest.getOrderItemDTOList();
        insertAfterSaleItemTable(orderId, orderItemDTOList, afterSaleId);

        //  3、新增售后变更表
        OrderInfoDTO orderInfoDTO = cancelOrderAssembleRequest.getOrderInfoDTO();
        insertCancelOrderAfterSaleLogTable(afterSaleId, orderInfoDTO,
                event.getFromStatus().getCode(),
                event.getToStatus().getCode()
        );

        //  4、新增售后支付表
        AfterSaleRefundDO afterSaleRefundDO = insertAfterSaleRefundTable(orderInfoDTO, afterSaleId, afterSaleInfoDO);
        cancelOrderAssembleRequest.setAfterSaleRefundId(afterSaleRefundDO.getId());
    }

    public CancelOrderRefundAmountDTO calculatingCancelOrderRefundAmount(CancelOrderAssembleRequest cancelOrderAssembleRequest) {
        OrderInfoDTO orderInfoDTO = cancelOrderAssembleRequest.getOrderInfoDTO();
        CancelOrderRefundAmountDTO cancelOrderRefundAmountDTO = new CancelOrderRefundAmountDTO();

        //  退整笔订单的实际支付金额
        cancelOrderRefundAmountDTO.setOrderId(orderInfoDTO.getOrderId());
        cancelOrderRefundAmountDTO.setTotalAmount(orderInfoDTO.getTotalAmount());
        cancelOrderRefundAmountDTO.setReturnGoodAmount(orderInfoDTO.getPayAmount());

        return cancelOrderRefundAmountDTO;
    }

    private void insertCancelOrderAfterSaleInfoTable(OrderInfoDO orderInfoDO, Integer cancelOrderAfterSaleStatus,
                                                     AfterSaleInfoDO afterSaleInfoDO, String afterSaleId) {
        afterSaleInfoDO.setAfterSaleId(afterSaleId);
        afterSaleInfoDO.setBusinessIdentifier(BusinessIdentifierEnum.SELF_MALL.getCode());
        afterSaleInfoDO.setOrderId(orderInfoDO.getOrderId());
        afterSaleInfoDO.setOrderSourceChannel(BusinessIdentifierEnum.SELF_MALL.getCode());
        afterSaleInfoDO.setUserId(orderInfoDO.getUserId());
        afterSaleInfoDO.setOrderType(OrderTypeEnum.NORMAL.getCode());
        afterSaleInfoDO.setApplyTime(new Date());
        afterSaleInfoDO.setAfterSaleStatus(cancelOrderAfterSaleStatus);

        afterSaleInfoDO.setRealRefundAmount(afterSaleInfoDO.getRealRefundAmount());
        afterSaleInfoDO.setApplyRefundAmount(afterSaleInfoDO.getApplyRefundAmount());
        //  取消订单 整笔退款
        afterSaleInfoDO.setAfterSaleType(AfterSaleTypeEnum.RETURN_MONEY.getCode());

        Integer cancelType = Integer.valueOf(orderInfoDO.getCancelType());
        if (OrderCancelTypeEnum.TIMEOUT_CANCELED.getCode().equals(cancelType)) {
            afterSaleInfoDO.setAfterSaleTypeDetail(AfterSaleTypeDetailEnum.TIMEOUT_NO_PAY.getCode());
            afterSaleInfoDO.setRemark("超时未支付自动取消");
        }
        if (OrderCancelTypeEnum.USER_CANCELED.getCode().equals(cancelType)) {
            afterSaleInfoDO.setAfterSaleTypeDetail(AfterSaleTypeDetailEnum.USER_CANCEL.getCode());
            afterSaleInfoDO.setRemark("用户手动取消");
        }
        afterSaleInfoDO.setApplyReasonCode(AfterSaleReasonEnum.CANCEL.getCode());
        afterSaleInfoDO.setApplyReason(AfterSaleReasonEnum.CANCEL.getMsg());
        afterSaleInfoDO.setApplySource(AfterSaleApplySourceEnum.SYSTEM.getCode());
        afterSaleInfoDO.setReviewTime(new Date());

        afterSaleInfoDAO.save(afterSaleInfoDO);

        log.info("新增订单售后记录,订单号:{},售后单号:{},订单售后状态:{}", afterSaleInfoDO.getOrderId(),
                afterSaleInfoDO.getAfterSaleId(), afterSaleInfoDO.getAfterSaleStatus());
    }

    private void insertAfterSaleItemTable(String orderId, List<OrderItemDTO> orderItemDTOList, String afterSaleId) {
        List<AfterSaleItemDO> itemDOList = Lists.newArrayList();
        for (OrderItemDTO orderItem : orderItemDTOList) {
            AfterSaleItemDO afterSaleItemDO = new AfterSaleItemDO();
            afterSaleItemDO.setAfterSaleId(afterSaleId);
            afterSaleItemDO.setOrderId(orderId);
            afterSaleItemDO.setSkuCode(orderItem.getSkuCode());
            afterSaleItemDO.setProductName(orderItem.getProductName());
            afterSaleItemDO.setProductImg(orderItem.getProductImg());
            afterSaleItemDO.setReturnQuantity(orderItem.getSaleQuantity());
            afterSaleItemDO.setOriginAmount(orderItem.getOriginAmount());
            afterSaleItemDO.setApplyRefundAmount(orderItem.getOriginAmount());
            afterSaleItemDO.setRealRefundAmount(orderItem.getPayAmount());
            //  取消订单 条目中的sku全部退货
            afterSaleItemDO.setReturnCompletionMark(AfterSaleReturnCompletionMarkEnum.ALL_RETURN_GOODS.getCode());
            afterSaleItemDO.setAfterSaleItemType(AfterSaleItemTypeEnum.AFTER_SALE_ORDER_ITEM.getCode());
            itemDOList.add(afterSaleItemDO);
        }
        afterSaleItemDAO.saveBatch(itemDOList);
    }

    private void insertCancelOrderAfterSaleLogTable(String afterSaleId, OrderInfoDTO orderInfoDTO,
                                                    Integer preAfterSaleStatus, Integer currentAfterSaleStatus) {

        AfterSaleLogDO afterSaleLogDO = new AfterSaleLogDO();
        afterSaleLogDO.setAfterSaleId(afterSaleId);
        afterSaleLogDO.setOrderId(orderInfoDTO.getOrderId());
        afterSaleLogDO.setPreStatus(preAfterSaleStatus);
        afterSaleLogDO.setCurrentStatus(currentAfterSaleStatus);

        //  取消订单的业务值
        Integer cancelType = Integer.valueOf(orderInfoDTO.getCancelType());
        if (OrderCancelTypeEnum.USER_CANCELED.getCode().equals(cancelType)) {
            afterSaleLogDO.setRemark(OrderCancelTypeEnum.USER_CANCELED.getMsg());
        }
        if (OrderCancelTypeEnum.TIMEOUT_CANCELED.getCode().equals(cancelType)) {
            afterSaleLogDO.setRemark(OrderCancelTypeEnum.TIMEOUT_CANCELED.getMsg());
        }

        afterSaleLogDAO.save(afterSaleLogDO);
        log.info("新增售后单变更信息, 订单号:{},售后单号:{},状态:PreStatus{},CurrentStatus:{}", orderInfoDTO.getOrderId(),
                afterSaleId, afterSaleLogDO.getPreStatus(), afterSaleLogDO.getCurrentStatus());
    }

    private AfterSaleRefundDO insertAfterSaleRefundTable(OrderInfoDTO orderInfoDTO, String afterSaleId, AfterSaleInfoDO afterSaleInfoDO) {
        String orderId = orderInfoDTO.getOrderId();
        AfterSaleRefundDO afterSaleRefundDO = orderPaymentDetailFactory.get(orderId, afterSaleId);
        afterSaleRefundDO.setRefundAmount(afterSaleInfoDO.getRealRefundAmount());

        afterSaleRefundDAO.save(afterSaleRefundDO);
        log.info("新增售后支付信息,订单号:{},售后单号:{},状态:{}", orderId, afterSaleId, afterSaleRefundDO.getRefundStatus());
        return afterSaleRefundDO;
    }

}
