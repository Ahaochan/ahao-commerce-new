package moe.ahao.commerce.order.application;


import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.commerce.market.api.command.LockUserCouponCommand;
import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.order.api.command.CreateOrderCommand;
import moe.ahao.commerce.order.infrastructure.component.OrderDataBuilder;
import moe.ahao.commerce.order.infrastructure.config.OrderProperties;
import moe.ahao.commerce.order.infrastructure.gateway.CouponGateway;
import moe.ahao.commerce.order.infrastructure.gateway.InventoryGateway;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.*;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service.*;
import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CreateOrderTxService {
    @Autowired
    private OrderProperties orderProperties;

    @Autowired
    private GenOrderIdAppService genOrderIdAppService;

    @Autowired
    private CouponGateway couponGateway;
    @Autowired
    private InventoryGateway inventoryGateway;

    @Autowired
    private OrderAmountDetailMybatisService orderAmountDetailMybatisService;
    @Autowired
    private OrderAmountMybatisService orderAmountMybatisService;
    @Autowired
    private OrderDeliveryDetailMybatisService orderDeliveryDetailMybatisService;
    @Autowired
    private OrderInfoMybatisService orderInfoMybatisService;
    @Autowired
    private OrderItemMybatisService orderItemMybatisService;
    @Autowired
    private OrderOperateLogMybatisService orderOperateLogMybatisService;
    @Autowired
    private OrderPaymentDetailMybatisService orderPaymentDetailMybatisService;
    @Autowired
    private OrderSnapshotMybatisService orderSnapshotMybatisService;

    @GlobalTransactional(rollbackFor = Exception.class)
    public String addNewOrder(CreateOrderCommand command, List<ProductSkuDTO> productList, CalculateOrderAmountDTO calculateOrderAmountDTO) {
        // 1. ???????????????
        this.lockUserCoupon(command.getOrderId(), command.getUserId(), command.getCouponId());
        // 2. ????????????
        this.deductProductStock(command.getOrderId(), command.getOrderItems());

        // 3. ???????????????????????????
        OrderDataBuilder.OrderData orderData = new OrderDataBuilder(command, productList, calculateOrderAmountDTO, orderProperties).build();
        // ??????????????????????????????????????????????????????????????????
        List<OrderDataBuilder.OrderData> subOrderDataList = orderData.split(list ->
            new ArrayList<>(list.stream()
                .collect(Collectors.groupingBy(OrderItemDO::getProductType, Collectors.toList()))
                .values()), genOrderIdAppService);
        List<OrderDataBuilder.OrderData> allOrderDataList = Stream.of(subOrderDataList, Collections.singletonList(orderData))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        // 4. ????????????????????????
        // ????????????
        List<OrderInfoDO> orderInfoList = allOrderDataList.stream()
            .map(OrderDataBuilder.OrderData::getOrderInfo)
            .collect(Collectors.toList());
        if (!orderInfoList.isEmpty()) {
            orderInfoMybatisService.saveBatch(orderInfoList);
        }

        // ????????????
        List<OrderItemDO> orderItemDOList = allOrderDataList.stream()
            .map(OrderDataBuilder.OrderData::getOrderItemList)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        if (!orderItemDOList.isEmpty()) {
            orderItemMybatisService.saveBatch(orderItemDOList);
        }

        // ??????????????????
        List<OrderDeliveryDetailDO> orderDeliveryDetailDOList = allOrderDataList.stream()
            .map(OrderDataBuilder.OrderData::getOrderDeliveryDetail)
            .collect(Collectors.toList());
        if (!orderDeliveryDetailDOList.isEmpty()) {
            orderDeliveryDetailMybatisService.saveBatch(orderDeliveryDetailDOList);
        }

        // ??????????????????
        List<OrderPaymentDetailDO> orderPaymentDetailDOList = allOrderDataList.stream()
            .map(OrderDataBuilder.OrderData::getOrderPaymentDetailList)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        if (!orderPaymentDetailDOList.isEmpty()) {
            orderPaymentDetailMybatisService.saveBatch(orderPaymentDetailDOList);
        }

        // ??????????????????
        List<OrderAmountDO> orderAmountDOList = allOrderDataList.stream()
            .map(OrderDataBuilder.OrderData::getOrderAmountList)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        if (!orderAmountDOList.isEmpty()) {
            orderAmountMybatisService.saveBatch(orderAmountDOList);
        }

        // ??????????????????
        List<OrderAmountDetailDO> orderAmountDetailDOList = allOrderDataList.stream()
            .map(OrderDataBuilder.OrderData::getOrderAmountDetailList)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        if (!orderAmountDetailDOList.isEmpty()) {
            orderAmountDetailMybatisService.saveBatch(orderAmountDetailDOList);
        }

        // ??????????????????????????????
        List<OrderOperateLogDO> orderOperateLogDOList = allOrderDataList.stream()
            .map(OrderDataBuilder.OrderData::getOrderOperateLog)
            .collect(Collectors.toList());
        if (!orderOperateLogDOList.isEmpty()) {
            orderOperateLogMybatisService.saveBatch(orderOperateLogDOList);
        }

        // ??????????????????
        List<OrderSnapshotDO> orderSnapshotDOList = allOrderDataList.stream()
            .map(OrderDataBuilder.OrderData::getOrderSnapshotList)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        if (!orderSnapshotDOList.isEmpty()) {
            orderSnapshotMybatisService.saveBatch(orderSnapshotDOList);
        }

        return command.getOrderId();
    }

    private void lockUserCoupon(String orderNo, String userNo, String couponNo) {
        if (StringUtils.isEmpty(couponNo)) {
            log.info("????????????, ??????{}???????????????{}?????????????????????{}, ??????????????????", userNo, orderNo, couponNo);
            return;
        }

        LockUserCouponCommand command = new LockUserCouponCommand();
        command.setUserId(command.getUserId());
        command.setCouponId(command.getCouponId());
        couponGateway.lock(command);
    }

    private void deductProductStock(String orderId, List<CreateOrderCommand.OrderItem> orderItems) {
        DeductProductStockCommand command = new DeductProductStockCommand();
        command.setOrderId(orderId);

        List<DeductProductStockCommand.OrderItem> orderItemCommandList = new ArrayList<>();
        for (CreateOrderCommand.OrderItem orderItem : orderItems) {
            DeductProductStockCommand.OrderItem orderItemCommand = new DeductProductStockCommand.OrderItem();
            orderItemCommand.setSkuCode(orderItem.getSkuCode());
            orderItemCommand.setSaleQuantity(orderItem.getSaleQuantity());

            orderItemCommandList.add(orderItemCommand);
        }
        command.setOrderItems(orderItemCommandList);

        inventoryGateway.deductProductStock(command);
    }
}
