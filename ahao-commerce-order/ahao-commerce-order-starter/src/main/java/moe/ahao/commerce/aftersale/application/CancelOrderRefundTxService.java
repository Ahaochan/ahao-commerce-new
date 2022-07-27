package moe.ahao.commerce.aftersale.application;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.infrastructure.enums.*;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleItemDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleLogDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleRefundDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleInfoMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleItemMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleLogMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleRefundMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.service.AfterSaleItemMybatisService;
import moe.ahao.commerce.common.enums.AfterSaleTypeDetailEnum;
import moe.ahao.commerce.common.enums.AfterSaleTypeEnum;
import moe.ahao.commerce.common.infrastructure.event.CancelOrderRefundEvent;
import moe.ahao.commerce.order.api.command.GenOrderIdCommand;
import moe.ahao.commerce.order.application.GenOrderIdAppService;
import moe.ahao.commerce.order.infrastructure.enums.AccountTypeEnum;
import moe.ahao.commerce.order.infrastructure.enums.OrderIdTypeEnum;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderItemDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderPaymentDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderItemMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderPaymentDetailMapper;
import moe.ahao.util.commons.lang.RandomHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class CancelOrderRefundTxService {
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
    private AfterSaleItemMapper afterSaleItemMapper;

    @Autowired
    private AfterSaleLogMapper afterSaleLogMapper;

    @Autowired
    private OrderPaymentDetailMapper orderPaymentDetailMapper;

    @Autowired
    private AfterSaleRefundMapper afterSaleRefundMapper;

    /**
     * 取消订单操作 记录售后信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CreateCancelOrderAfterSaleDTO insertCancelOrderAfterSale(CancelOrderRefundEvent event) {
        String orderId = event.getOrderId();
        OrderInfoDO orderInfo = orderInfoMapper.selectOneByOrderId(orderId);
        List<OrderItemDO> orderItems = orderItemMapper.selectListByOrderId(orderId);

        // 取消订单过程中的 申请退款金额 和 实际退款金额 都是实付退款金额, 金额相同
        // 1. 新增售后订单表
        String afterSaleId = this.generateAfterSaleId(orderInfo);
        Integer fromStatus = AfterSaleStatusEnum.UN_CREATED.getCode();
        Integer toStatus = AfterSaleStatusEnum.REVIEW_PASS.getCode();
        AfterSaleInfoDO afterSaleInfoDO = this.buildAfterSaleInfo(orderInfo, afterSaleId, toStatus);
        afterSaleInfoMapper.insert(afterSaleInfoDO);
        log.info("新增订单售后记录, 订单号:{}, 售后单号:{}, 订单售后状态:{}", orderId, afterSaleId, afterSaleInfoDO.getAfterSaleStatus());

        // 2. 新增售后条目表
        List<AfterSaleItemDO> afterSaleItems = this.buildAfterSaleItems(afterSaleId, orderItems);
        afterSaleItemMybatisService.saveBatch(afterSaleItems);

        // 3. 新增售后变更表
        Integer cancelType = event.getCancelType();
        AfterSaleLogDO afterSaleLogDO = this.buildAfterSaleLog(afterSaleId, fromStatus, toStatus, cancelType);
        afterSaleLogMapper.insert(afterSaleLogDO);
        log.info("新增售后单变更信息, 订单号:{}, 售后单号:{}, preStatus:{}, currentStatus:{}", orderId, afterSaleId, afterSaleLogDO.getPreStatus(), afterSaleLogDO.getCurrentStatus());

        // 4. 新增售后支付表
        AfterSaleRefundDO afterSaleRefundDO = this.buildAfterSaleRefund(afterSaleInfoDO);
        afterSaleRefundMapper.insert(afterSaleRefundDO);
        log.info("新增售后支付信息,订单号:{},售后单号:{},状态:{}", orderId, afterSaleId, afterSaleRefundDO.getRefundStatus());

        String afterSaleRefundId = afterSaleRefundDO.getAfterSaleRefundId();
        CreateCancelOrderAfterSaleDTO dto = new CreateCancelOrderAfterSaleDTO();
        dto.setAfterSaleId(afterSaleId);
        dto.setAfterSaleId(afterSaleRefundId);
        return dto;
    }

    private String generateAfterSaleId(OrderInfoDO orderInfo) {
        Integer businessIdentifier = orderInfo.getBusinessIdentifier();
        String userId = orderInfo.getUserId();
        GenOrderIdCommand command = new GenOrderIdCommand(businessIdentifier, OrderIdTypeEnum.AFTER_SALE.getCode(), userId);
        String afterSaleId = genOrderIdAppService.generate(command);
        return afterSaleId;
    }

    private AfterSaleInfoDO buildAfterSaleInfo(OrderInfoDO orderInfoDO, String afterSaleId, Integer cancelOrderAfterSaleStatus) {
        AfterSaleInfoDO afterSaleInfoDO = new AfterSaleInfoDO();
        afterSaleInfoDO.setAfterSaleId(afterSaleId);
        afterSaleInfoDO.setBusinessIdentifier(orderInfoDO.getBusinessIdentifier());
        afterSaleInfoDO.setOrderId(orderInfoDO.getOrderId());
        afterSaleInfoDO.setUserId(orderInfoDO.getUserId());
        afterSaleInfoDO.setOrderType(orderInfoDO.getOrderType());
        afterSaleInfoDO.setApplySource(AfterSaleApplySourceEnum.SYSTEM.getCode());
        afterSaleInfoDO.setApplyTime(new Date());
        afterSaleInfoDO.setApplyReasonCode(AfterSaleReasonEnum.CANCEL.getCode());
        afterSaleInfoDO.setApplyReason(AfterSaleReasonEnum.CANCEL.getName());
        afterSaleInfoDO.setReviewTime(new Date());
        // 取消订单不需要审核, 自动审核通过
        // afterSaleInfoDO.setReviewSource();
        // afterSaleInfoDO.setReviewReasonCode();
        // afterSaleInfoDO.setReviewReason();
        //  取消订单 整笔退款
        afterSaleInfoDO.setAfterSaleType(AfterSaleTypeEnum.RETURN_MONEY.getCode());
        // afterSaleInfoDO.setAfterSaleTypeDetail();
        afterSaleInfoDO.setAfterSaleStatus(cancelOrderAfterSaleStatus);
        afterSaleInfoDO.setApplyRefundAmount(orderInfoDO.getPayAmount());
        afterSaleInfoDO.setRealRefundAmount(orderInfoDO.getPayAmount());
        // afterSaleInfoDO.setRemark();

        Integer cancelType = orderInfoDO.getCancelType();
        OrderCancelTypeEnum orderCancelTypeEnum = OrderCancelTypeEnum.getByCode(cancelType);
        if (OrderCancelTypeEnum.TIMEOUT_CANCELED == orderCancelTypeEnum) {
            afterSaleInfoDO.setAfterSaleTypeDetail(AfterSaleTypeDetailEnum.TIMEOUT_NO_PAY.getCode());
            afterSaleInfoDO.setRemark("超时未支付自动取消");
        }
        if (OrderCancelTypeEnum.USER_CANCELED == orderCancelTypeEnum) {
            afterSaleInfoDO.setAfterSaleTypeDetail(AfterSaleTypeDetailEnum.USER_CANCEL.getCode());
            afterSaleInfoDO.setRemark("用户手动取消");
        }
        return afterSaleInfoDO;
    }

    private List<AfterSaleItemDO> buildAfterSaleItems(String afterSaleId, List<OrderItemDO> orderItems) {
        List<AfterSaleItemDO> afterSaleItems = new ArrayList<>(orderItems.size());
        for (OrderItemDO orderItem : orderItems) {
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

    private AfterSaleLogDO buildAfterSaleLog(String afterSaleId, Integer fromStatus, Integer toStatus, Integer cancelType) {
        AfterSaleLogDO afterSaleLogDO = new AfterSaleLogDO();
        afterSaleLogDO.setAfterSaleId(afterSaleId);
        afterSaleLogDO.setPreStatus(fromStatus);
        afterSaleLogDO.setCurrentStatus(toStatus);

        // 取消订单类型
        OrderCancelTypeEnum orderCancelTypeEnum = OrderCancelTypeEnum.getByCode(cancelType);
        if (orderCancelTypeEnum != null) {
            afterSaleLogDO.setRemark(orderCancelTypeEnum.getName());
        }
        return afterSaleLogDO;
    }

    private AfterSaleRefundDO buildAfterSaleRefund(AfterSaleInfoDO afterSaleInfo) {
        String orderId = afterSaleInfo.getOrderId();
        OrderPaymentDetailDO paymentDetail = orderPaymentDetailMapper.selectOneByOrderId(orderId);

        AfterSaleRefundDO afterSaleRefundDO = new AfterSaleRefundDO();
        afterSaleRefundDO.setAfterSaleId(afterSaleInfo.getAfterSaleId());
        afterSaleRefundDO.setOrderId(orderId);
        afterSaleRefundDO.setAccountType(AccountTypeEnum.THIRD.getCode());
        afterSaleRefundDO.setRefundStatus(RefundStatusEnum.UN_REFUND.getCode());
        afterSaleRefundDO.setRemark(RefundStatusEnum.UN_REFUND.getName());
        afterSaleRefundDO.setRefundAmount(afterSaleInfo.getRealRefundAmount());
        afterSaleRefundDO.setAfterSaleBatchNo(orderId + RandomHelper.getString(10, RandomHelper.DIST_NUMBER));

        if (paymentDetail != null) {
            afterSaleRefundDO.setOutTradeNo(paymentDetail.getOutTradeNo());
            afterSaleRefundDO.setPayType(paymentDetail.getPayType());
        }

        return afterSaleRefundDO;
    }

    @Data
    public static class CreateCancelOrderAfterSaleDTO {
        private String afterSaleId;
        private String afterSaleRefundId;
    }
}
