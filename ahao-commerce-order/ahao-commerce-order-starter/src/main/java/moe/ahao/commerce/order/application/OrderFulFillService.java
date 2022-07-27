package moe.ahao.commerce.order.application;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.enums.AmountTypeEnum;
import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.fulfill.api.command.ReceiveFulfillCommand;
import moe.ahao.commerce.fulfill.api.command.ReceiveOrderItemCommand;
import moe.ahao.commerce.order.infrastructure.component.OrderOperateLogFactory;
import moe.ahao.commerce.order.infrastructure.domain.dto.WmsShipDTO;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.*;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.*;
import moe.ahao.commerce.order.infrastructure.wms.OrderDeliveredProcessor;
import moe.ahao.commerce.order.infrastructure.wms.OrderOutStockedProcessor;
import moe.ahao.commerce.order.infrastructure.wms.OrderSignedProcessor;
import moe.ahao.commerce.order.infrastructure.wms.OrderWmsShipResultProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单履约相关service
 */
@Slf4j
@Service
public class OrderFulFillService implements ApplicationContextAware {
    @Setter
    private ApplicationContext applicationContext;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderAmountMapper orderAmountMapper;

    @Autowired
    private OrderDeliveryDetailMapper orderDeliveryDetailMapper;

    @Autowired
    private OrderOperateLogFactory orderOperateLogFactory;

    @Autowired
    private OrderOperateLogMapper orderOperateLogMapper;

    /**
     * 触发订单进行履约流程
     */
    @Transactional(rollbackFor = Exception.class)
    public void triggerOrderFulFill(String orderId) {
        // 1. 查询订单
        OrderInfoDO order = orderInfoMapper.selectOneByOrderId(orderId);
        if (order == null) {
            return;
        }

        // 2. 校验订单是否已支付
        OrderStatusEnum orderStatus = OrderStatusEnum.getByCode(order.getOrderStatus());
        if (OrderStatusEnum.PAID != orderStatus) {
            log.info("订单未支付, 无法进行履约, orderId={}", order.getOrderId());
            return;
        }

        // 3. 更新订单状态为已履约
        orderInfoMapper.updateOrderStatusByOrderId(orderId, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.FULFILL.getCode());

        // 4. 并插入一条订单变更记录
        OrderOperateLogDO orderOperateLog = orderOperateLogFactory.get(order, OrderStatusChangeEnum.ORDER_FULFILLED);
        orderOperateLogMapper.insert(orderOperateLog);
    }

    /**
     * 通知订单物流配送结果接口
     */
    public void informOrderWmsShipResult(WmsShipDTO wmsShipDTO) throws OrderException {
        // 1. 获取对应的订单物流结果处理器
        OrderWmsShipResultProcessor processor = getProcessor(wmsShipDTO.getStatusChange());

        // 2. 执行
        if (null != processor) {
            processor.execute(wmsShipDTO);
        }
    }

    /**
     * 构建接受订单履约请求
     */
    public ReceiveFulfillCommand buildReceiveFulFillRequest(OrderInfoDO orderInfo) {
        OrderDeliveryDetailDO orderDeliveryDetail = orderDeliveryDetailMapper.selectOneByOrderId(orderInfo.getOrderId());
        List<OrderItemDO> orderItems = orderItemMapper.selectListByOrderId(orderInfo.getOrderId());
        OrderAmountDO deliveryAmount = orderAmountMapper.selectOneByOrderIdAndAmountType(orderInfo.getOrderId(), AmountTypeEnum.SHIPPING_AMOUNT.getCode());

        // 构造请求
        ReceiveFulfillCommand request = new ReceiveFulfillCommand();
        request.setBusinessIdentifier(orderInfo.getBusinessIdentifier());
        request.setOrderId(orderInfo.getOrderId());
        request.setSellerId(orderInfo.getSellerId());
        request.setUserId(orderInfo.getUserId());
        request.setDeliveryType(orderDeliveryDetail.getDeliveryType());
        request.setReceiverName(orderDeliveryDetail.getReceiverName());
        request.setReceiverPhone(orderDeliveryDetail.getReceiverPhone());
        request.setReceiverProvince(orderDeliveryDetail.getProvince());
        request.setReceiverCity(orderDeliveryDetail.getCity());
        request.setReceiverArea(orderDeliveryDetail.getArea());
        request.setReceiverStreet(orderDeliveryDetail.getStreet());
        request.setReceiverDetailAddress(orderDeliveryDetail.getDetailAddress());
        request.setReceiverLat(orderDeliveryDetail.getLat());
        request.setReceiverLon(orderDeliveryDetail.getLon());
        request.setPayType(orderInfo.getPayType());
        request.setPayAmount(orderInfo.getPayAmount());
        request.setTotalAmount(orderInfo.getTotalAmount());
        request.setReceiveOrderItems(this.buildReceiveOrderItemRequest(orderInfo, orderItems));

        // 运费
        if (deliveryAmount != null) {
            request.setDeliveryAmount(deliveryAmount.getAmount());
        }
        return request;
    }


    private List<ReceiveOrderItemCommand> buildReceiveOrderItemRequest(OrderInfoDO orderInfo, List<OrderItemDO> items) {
        List<ReceiveOrderItemCommand> itemRequests = new ArrayList<>();
        for (OrderItemDO item : items) {
            ReceiveOrderItemCommand request = new ReceiveOrderItemCommand();
            request.setSkuCode(item.getSkuCode());
            request.setProductName(item.getProductName());
            request.setSalePrice(item.getSalePrice());
            request.setSaleQuantity(item.getSaleQuantity());
            request.setProductUnit(item.getProductUnit());
            request.setPayAmount(item.getPayAmount());
            request.setOriginAmount(item.getOriginAmount());

            itemRequests.add(request);
        }
        return itemRequests;
    }

    /**
     * 获取对应的订单物流结果处理器
     *
     * @param orderStatusChange
     * @return
     */
    private OrderWmsShipResultProcessor getProcessor(OrderStatusChangeEnum orderStatusChange) {

        if (OrderStatusChangeEnum.ORDER_OUT_STOCKED.equals(orderStatusChange)) {
            return applicationContext.getBean(OrderOutStockedProcessor.class);
        } else if (OrderStatusChangeEnum.ORDER_DELIVERED.equals(orderStatusChange)) {
            return applicationContext.getBean(OrderDeliveredProcessor.class);
        } else if (OrderStatusChangeEnum.ORDER_SIGNED.equals(orderStatusChange)) {
            return applicationContext.getBean(OrderSignedProcessor.class);
        }

        return null;
    }
}
