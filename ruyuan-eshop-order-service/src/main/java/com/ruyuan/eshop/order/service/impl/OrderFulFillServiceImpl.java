package com.ruyuan.eshop.order.service.impl;

import com.ruyuan.eshop.common.enums.AmountTypeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveOrderItemRequest;
import com.ruyuan.eshop.order.dao.OrderAmountDAO;
import com.ruyuan.eshop.order.dao.OrderDeliveryDetailDAO;
import com.ruyuan.eshop.order.dao.OrderItemDAO;
import com.ruyuan.eshop.order.domain.dto.AfterFulfillDTO;
import com.ruyuan.eshop.order.domain.entity.OrderAmountDO;
import com.ruyuan.eshop.order.domain.entity.OrderDeliveryDetailDO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.domain.entity.OrderItemDO;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.service.OrderFulFillService;
import com.ruyuan.eshop.order.statemachine.StateMachineFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 订单履约相关service
 * </p>
 *
 * @author zhonghuashishan
 */
@Slf4j
@Service
public class OrderFulFillServiceImpl implements OrderFulFillService {


    @Autowired
    private OrderItemDAO orderItemDAO;
    @Autowired
    private OrderAmountDAO orderAmountDAO;

    @Autowired
    private OrderDeliveryDetailDAO orderDeliveryDetailDAO;

    @Resource
    private StateMachineFactory stateMachineFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void triggerOrderFulFill(String orderId) throws OrderBizException {
        // 状态机流转
        OrderStatusChangeEnum event = OrderStatusChangeEnum.ORDER_FULFILLED;
        StateMachineFactory.OrderStateMachine orderStateMachine = stateMachineFactory.getOrderStateMachine(event.getFromStatus());
        AfterFulfillDTO afterFulfillDTO = new AfterFulfillDTO();
        afterFulfillDTO.setOrderId(orderId);
        afterFulfillDTO.setStatusChange(event);
        orderStateMachine.fire(event, afterFulfillDTO);
    }

    @Override
    public void informOrderAfterFulfillResult(AfterFulfillDTO afterFulfillDTO) throws OrderBizException {
        // 状态机流转
        OrderStatusChangeEnum event = afterFulfillDTO.getStatusChange();
        StateMachineFactory.OrderStateMachine orderStateMachine = stateMachineFactory.getOrderStateMachine(event.getFromStatus());
        orderStateMachine.fire(event, afterFulfillDTO);
    }

    /**
     * 构建接受订单履约请求
     */
    @Override
    public ReceiveFulfillRequest buildReceiveFulFillRequest(OrderInfoDO orderInfo) {
        OrderDeliveryDetailDO orderDeliveryDetail = orderDeliveryDetailDAO.getByOrderId(orderInfo.getOrderId());
        List<OrderItemDO> orderItems = orderItemDAO.listByOrderId(orderInfo.getOrderId());
        OrderAmountDO deliveryAmount = orderAmountDAO.getOne(orderInfo.getOrderId()
                , AmountTypeEnum.SHIPPING_AMOUNT.getCode());

        //构造请求
        ReceiveFulfillRequest request = ReceiveFulfillRequest.builder()
                .businessIdentifier(orderInfo.getBusinessIdentifier())
                .orderId(orderInfo.getOrderId())
                .orderType(orderInfo.getOrderType())
                .sellerId(orderInfo.getSellerId())
                .userId(orderInfo.getUserId())
                .deliveryType(orderDeliveryDetail.getDeliveryType())
                .receiverName(orderDeliveryDetail.getReceiverName())
                .receiverPhone(orderDeliveryDetail.getReceiverPhone())
                .receiverProvince(orderDeliveryDetail.getProvince())
                .receiverCity(orderDeliveryDetail.getCity())
                .receiverArea(orderDeliveryDetail.getArea())
                .receiverStreet(orderDeliveryDetail.getStreet())
                .receiverDetailAddress(orderDeliveryDetail.getDetailAddress())
                .receiverLat(orderDeliveryDetail.getLat())
                .receiverLon(orderDeliveryDetail.getLon())
                .payType(orderInfo.getPayType())
                .payAmount(orderInfo.getPayAmount())
                .totalAmount(orderInfo.getTotalAmount())
                .receiveOrderItems(buildReceiveOrderItemRequest(orderItems))
                .build();

        //运费
        if (null != deliveryAmount) {
            request.setDeliveryAmount(deliveryAmount.getAmount());
        }
        return request;
    }


    private List<ReceiveOrderItemRequest> buildReceiveOrderItemRequest(List<OrderItemDO> items) {
        List<ReceiveOrderItemRequest> itemRequests = new ArrayList<>();
        items.forEach(item -> {
            ReceiveOrderItemRequest request = ReceiveOrderItemRequest.builder()
                    .skuCode(item.getSkuCode())
                    .productType(item.getProductType())
                    .productName(item.getProductName())
                    .salePrice(item.getSalePrice())
                    .saleQuantity(item.getSaleQuantity())
                    .productUnit(item.getProductUnit())
                    .payAmount(item.getPayAmount())
                    .originAmount(item.getOriginAmount())
                    .extJson(item.getExtJson())
                    .build();
            itemRequests.add(request);
        });

        return itemRequests;
    }
}
