package com.ruyuan.eshop.order.converter;

import com.ruyuan.eshop.order.domain.dto.*;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.domain.query.AcceptOrderQuery;
import com.ruyuan.eshop.order.domain.query.OrderQuery;
import com.ruyuan.eshop.order.domain.request.*;
import moe.ahao.commerce.customer.api.command.CustomerReceiveAfterSaleCommand;
import moe.ahao.commerce.fulfill.api.command.CancelFulfillCommand;
import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.commerce.inventory.api.event.ReleaseProductStockEvent;
import moe.ahao.commerce.market.api.command.LockUserCouponCommand;
import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.market.api.query.CalculateOrderAmountQuery;
import moe.ahao.commerce.pay.api.command.PayOrderCommand;
import moe.ahao.commerce.pay.api.dto.PayOrderDTO;
import moe.ahao.commerce.risk.api.command.CheckOrderRiskCommand;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface OrderConverter {

    /**
     * 对象转换
     *
     * @param orderInfo 对象
     * @return 对象
     */
    OrderInfoDTO orderInfoDO2DTO(OrderInfoDO orderInfo);

    /**
     * 对象转换
     *
     * @param orderItem 对象
     * @return 对象
     */
    OrderItemDTO orderItemDO2DTO(OrderItemDO orderItem);

    /**
     * 对象转换
     *
     * @param orderItem 对象
     * @return 对象
     */
    List<OrderItemDTO> orderItemDO2DTO(List<OrderItemDO> orderItem);

    /**
     * 对象转换
     *
     * @param orderAmountDetail 对象
     * @return 对象
     */
    OrderAmountDetailDTO orderAmountDetailDO2DTO(OrderAmountDetailDO orderAmountDetail);

    /**
     * 对象转换
     *
     * @param orderAmountDetails 对象
     * @return 对象
     */
    List<OrderAmountDetailDTO> orderAmountDetailDO2DTO(List<OrderAmountDetailDO> orderAmountDetails);

    /**
     * 对象转换
     *
     * @param orderAmountDetail 对象
     * @return 对象
     */
    OrderDeliveryDetailDTO orderDeliveryDetailDO2DTO(OrderDeliveryDetailDO orderAmountDetail);

    /**
     * 对象转换
     *
     * @param orderPaymentDetail 对象
     * @return 对象
     */
    OrderPaymentDetailDTO orderPaymentDetailDO2DTO(OrderPaymentDetailDO orderPaymentDetail);

    /**
     * 对象转换
     *
     * @param orderPaymentDetails 对象
     * @return 对象
     */
    List<OrderPaymentDetailDTO> orderPaymentDetailDO2DTO(List<OrderPaymentDetailDO> orderPaymentDetails);

    /**
     * 对象转换
     *
     * @param orderOperateLog 对象
     * @return 对象
     */
    OrderOperateLogDTO orderOperateLogsDO2DTO(OrderOperateLogDO orderOperateLog);

    /**
     * 对象转换
     *
     * @param orderOperateLogs 对象
     * @return 对象
     */
    List<OrderOperateLogDTO> orderOperateLogsDO2DTO(List<OrderOperateLogDO> orderOperateLogs);

    /**
     * 对象转换
     *
     * @param orderSnapshot 对象
     * @return 对象
     */
    OrderSnapshotDTO orderSnapshotsDO2DTO(OrderSnapshotDO orderSnapshot);

    /**
     * 对象转换
     *
     * @param orderSnapshots 对象
     * @return 对象
     */
    List<OrderSnapshotDTO> orderSnapshotsDO2DTO(List<OrderSnapshotDO> orderSnapshots);

    /**
     * 对象转换
     *
     * @param lackItem 对象
     * @return 对象
     */
    OrderLackItemDTO lackItemDO2DTO(OrderLackItemDTO lackItem);

    /**
     * 对象转换
     *
     * @param orderAmountDetail 对象
     * @return 对象
     */
    OrderAmountDetailDTO convertOrderAmountDetail(CalculateOrderAmountDTO.OrderItemAmountDTO orderAmountDetail);

    /**
     * 对象转换
     *
     * @param orderAmountDetail 对象
     * @return 对象
     */
    List<OrderAmountDetailDTO> convertOrderAmountDetail(List<CalculateOrderAmountDTO.OrderItemAmountDTO> orderAmountDetail);

    /**
     * 对象转换
     *
     * @param orderAmountList 对象
     * @return 对象
     */
    OrderAmountDTO convertOrderAmountDTO(CalculateOrderAmountDTO.OrderAmountDTO orderAmountList);

    /**
     * 对象转换
     *
     * @param orderAmountList 对象
     * @return 对象
     */
    List<OrderAmountDTO> convertOrderAmountDTO(List<CalculateOrderAmountDTO.OrderAmountDTO> orderAmountList);

    /**
     * 对象转换
     *
     * @param releaseProductOrderItemRequest 对象
     * @return 对象
     */
    ReleaseProductStockEvent.OrderItem convertOrderItemRequest(ReleaseProductStockDTO.OrderItemRequest releaseProductOrderItemRequest);

    /**
     * 对象转换
     *
     * @param query 对象
     * @return 对象
     */
    OrderListQueryDTO orderListQuery2DTO(OrderQuery query);

    /**
     * 对象转换
     *
     * @param createOrderRequest 对象
     * @return 对象
     */
    LockUserCouponCommand convertLockUserCouponRequest(CreateOrderRequest createOrderRequest);

    /**
     * 对象转换
     *
     * @param orderInfoDO 对象
     * @return 对象
     */
    OrderInfoDO copyOrderInfoDTO(OrderInfoDO orderInfoDO);

    /**
     * 对象转换
     *
     * @param orderDeliveryDetailDO 对象
     * @return 对象
     */
    OrderDeliveryDetailDO copyOrderDeliverDetailDO(OrderDeliveryDetailDO orderDeliveryDetailDO);

    /**
     * 对象转换
     *
     * @param orderItemDO 对象
     * @return 对象
     */
    OrderItemDO copyOrderItemDO(OrderItemDO orderItemDO);

    /**
     * 对象转换
     *
     * @param orderAmountDetailDO 对象
     * @return 对象
     */
    OrderAmountDetailDO copyOrderAmountDetail(OrderAmountDetailDO orderAmountDetailDO);

    /**
     * 对象转换
     *
     * @param orderAmountDO 对象
     * @return 对象
     */
    OrderAmountDO copyOrderAmountDO(OrderAmountDO orderAmountDO);

    /**
     * 对象转换
     *
     * @param orderPaymentDetailDO 对象
     * @return 对象
     */
    OrderPaymentDetailDO copyOrderPaymentDetailDO(OrderPaymentDetailDO orderPaymentDetailDO);

    /**
     * 对象转换
     *
     * @param orderOperateLogDO 对象
     * @return 对象
     */
    OrderOperateLogDO copyOrderOperationLogDO(OrderOperateLogDO orderOperateLogDO);

    /**
     * 对象转换
     *
     * @param orderSnapshotDO 对象
     * @return 对象
     */
    OrderSnapshotDO copyOrderSnapshot(OrderSnapshotDO orderSnapshotDO);

    /**
     * 对象转换
     *
     * @param orderInfoDTO 对象
     * @return 对象
     */
    CancelFulfillCommand convertCancelFulfillRequest(OrderInfoDTO orderInfoDTO);

    /**
     * 对象转换
     *
     * @param orderInfoDTO 对象
     * @return 对象
     */
    OrderInfoDO orderInfoDTO2DO(OrderInfoDTO orderInfoDTO);

    /**
     * 对象转换
     *
     * @param cancelOrderRequest 对象
     * @return 对象
     */
    CancelOrderAssembleRequest convertCancelOrderRequest(CancelOrderRequest cancelOrderRequest);

    /**
     * 对象转换
     *
     * @param returnGoodsAssembleRequest 对象
     * @return 对象
     */
    CustomerReceiveAfterSaleCommand convertReturnGoodsAssembleRequest(ReturnGoodsAssembleRequest returnGoodsAssembleRequest);

    /**
     * 对象转换
     *
     * @param returnGoodsOrderRequest 对象
     * @return 对象
     */
    ReturnGoodsAssembleRequest returnGoodRequest2AssembleRequest(ReturnGoodsOrderRequest returnGoodsOrderRequest);

    /**
     * 对象转换
     *
     * @param orderItemRequest 对象
     * @return 对象
     */
    DeductProductStockCommand.OrderItem convertOrderItemRequest(CreateOrderRequest.OrderItemRequest orderItemRequest);

    /**
     * 对象转换
     *
     * @param orderItemRequestList 对象
     * @return 对象
     */
    List<DeductProductStockCommand.OrderItem> convertOrderItemRequest(List<CreateOrderRequest.OrderItemRequest> orderItemRequestList);

    /**
     * 对象转换
     *
     * @param createOrderRequest 对象
     * @return 对象
     */
    CheckOrderRiskCommand convertRiskRequest(CreateOrderRequest createOrderRequest);

    /**
     * 对象转换
     *
     * @param createOrderRequest 对象
     * @return 对象
     */
    CalculateOrderAmountQuery convertCalculateOrderAmountRequest(CreateOrderRequest createOrderRequest);

    /**
     * 对象转换
     *
     * @param prePayOrderRequest 对象
     * @return 对象
     */
    PayOrderCommand convertPayOrderRequest(PrePayOrderRequest prePayOrderRequest);

    /**
     * 对象转换
     *
     * @param payOrderDTO 对象
     * @return 对象
     */
    PrePayOrderDTO convertPrePayOrderRequest(PayOrderDTO payOrderDTO);

    /**
     * 对象转换
     *
     * @param acceptOrderQuery 对象
     * @return 对象
     */
    OrderQuery convertAcceptOrderQuery(AcceptOrderQuery acceptOrderQuery);

}
