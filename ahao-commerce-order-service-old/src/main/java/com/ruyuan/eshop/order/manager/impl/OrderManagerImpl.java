package com.ruyuan.eshop.order.manager.impl;

import com.ruyuan.eshop.common.enums.AmountTypeEnum;
import com.ruyuan.eshop.common.enums.OrderOperateTypeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import com.ruyuan.eshop.common.utils.LoggerFormat;
import com.ruyuan.eshop.order.builder.FullOrderData;
import com.ruyuan.eshop.order.builder.NewOrderBuilder;
import com.ruyuan.eshop.order.config.OrderProperties;
import com.ruyuan.eshop.order.converter.OrderConverter;
import com.ruyuan.eshop.order.dao.*;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.domain.request.CreateOrderRequest;
import com.ruyuan.eshop.order.enums.OrderNoTypeEnum;
import com.ruyuan.eshop.order.enums.PayStatusEnum;
import com.ruyuan.eshop.order.enums.SnapshotTypeEnum;
import com.ruyuan.eshop.order.manager.OrderManager;
import com.ruyuan.eshop.order.manager.OrderNoManager;
import com.ruyuan.eshop.order.remote.AddressRemote;
import com.ruyuan.eshop.order.remote.InventoryRemote;
import com.ruyuan.eshop.order.remote.MarketRemote;
import com.ruyuan.eshop.order.service.impl.NewOrderDataHolder;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.address.api.dto.AddressFullDTO;
import moe.ahao.commerce.address.api.query.AddressQuery;
import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.commerce.market.api.command.LockUserCouponCommand;
import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.market.api.dto.UserCouponDTO;
import moe.ahao.commerce.market.api.query.GetUserCouponQuery;
import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class OrderManagerImpl implements OrderManager {

    @Autowired
    private OrderInfoDAO orderInfoDAO;

    @Autowired
    private OrderItemDAO orderItemDAO;

    @Autowired
    private OrderPaymentDetailDAO orderPaymentDetailDAO;

    @Autowired
    private OrderOperateLogDAO orderOperateLogDAO;

    @Autowired
    private OrderAmountDAO orderAmountDAO;

    @Autowired
    private OrderAmountDetailDAO orderAmountDetailDAO;

    @Autowired
    private OrderDeliveryDetailDAO orderDeliveryDetailDAO;

    @Autowired
    private OrderSnapshotDAO orderSnapshotDAO;

    @Autowired
    private OrderProperties orderProperties;

    /**
     * 营销服务
     */
    @Autowired
    private MarketRemote marketRemote;

    /**
     * 地址服务
     */
    @Autowired
    private AddressRemote addressRemote;

    @Autowired
    private OrderNoManager orderNoManager;

    /**
     * 库存服务
     */
    @Autowired
    private InventoryRemote inventoryRemote;

    @Autowired
    private OrderConverter orderConverter;

    /**
     * 生成订单
     *
     * @param createOrderRequest
     * @param productSkuList
     * @param calculateOrderAmountDTO
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createOrder(CreateOrderRequest createOrderRequest, List<ProductSkuDTO> productSkuList, CalculateOrderAmountDTO calculateOrderAmountDTO) {
        // 锁定优惠券
        lockUserCoupon(createOrderRequest);

        log.info(LoggerFormat.build()
                .remark("OrderManager.createOrder-> before deduct stock")
                .finish());
        // 扣减库存
        deductProductStock(createOrderRequest);

        log.info(LoggerFormat.build()
                .remark("OrderManager.createOrder-> after deduct stock")
                .finish());

        // 生成订单到数据库
        addNewOrder(createOrderRequest, productSkuList, calculateOrderAmountDTO);
    }


    /**
     * 锁定用户优惠券
     */
    private void lockUserCoupon(CreateOrderRequest createOrderRequest) {
        String couponId = createOrderRequest.getCouponId();
        if (StringUtils.isEmpty(couponId)) {
            return;
        }
        LockUserCouponCommand lockUserCouponRequest = orderConverter.convertLockUserCouponRequest(createOrderRequest);
        // 调用营销服务锁定用户优惠券
        marketRemote.lockUserCoupon(lockUserCouponRequest);
    }

    /**
     * 锁定商品库存
     *
     * @param createOrderRequest 订单信息
     */
    private void deductProductStock(CreateOrderRequest createOrderRequest) {
        String orderId = createOrderRequest.getOrderId();
        List<DeductProductStockCommand.OrderItem> orderItemRequestList =
                orderConverter.convertOrderItemRequest(createOrderRequest.getOrderItemRequestList());
        DeductProductStockCommand lockProductStockRequest = new DeductProductStockCommand();
        lockProductStockRequest.setOrderId(orderId);
        lockProductStockRequest.setOrderItems(orderItemRequestList);
        inventoryRemote.deductProductStock(lockProductStockRequest);
    }

    /**
     * 新增订单数据到数据库
     */
    private void addNewOrder(CreateOrderRequest createOrderRequest, List<ProductSkuDTO> productSkuList, CalculateOrderAmountDTO calculateOrderAmountDTO) {
        String orderId = createOrderRequest.getOrderId();
        // 封装新订单数据
        NewOrderDataHolder newOrderDataHolder = new NewOrderDataHolder();

        // 生成主订单
        FullOrderData fullMasterOrderData = addNewMasterOrder(createOrderRequest, productSkuList, calculateOrderAmountDTO);

        // 封装主订单数据到NewOrderData对象中
        newOrderDataHolder.appendOrderData(fullMasterOrderData);


        // 如果存在多种商品类型，需要按商品类型进行拆单
        Map<Integer, List<ProductSkuDTO>> productTypeMap = productSkuList.stream().collect(Collectors.groupingBy(ProductSkuDTO::getProductType));
        if (productTypeMap.keySet().size() > 1) {
            for (Integer productType : productTypeMap.keySet()) {
                // 生成子订单
                FullOrderData fullSubOrderData = addNewSubOrder(fullMasterOrderData, productType);

                // 封装子订单数据到NewOrderData对象中
                newOrderDataHolder.appendOrderData(fullSubOrderData);
            }
        }

        // 保存订单到数据库
        // 订单信息
        List<OrderInfoDO> orderInfoDOList = newOrderDataHolder.getOrderInfoDOList();
        if (!orderInfoDOList.isEmpty()) {
            log.info(LoggerFormat.build()
                    .remark("保存订单信息")
                    .data("orderId", orderId)
                    .finish());
            orderInfoDAO.saveBatch(orderInfoDOList);
        }

        // 订单条目
        List<OrderItemDO> orderItemDOList = newOrderDataHolder.getOrderItemDOList();
        if (!orderItemDOList.isEmpty()) {
            log.info(LoggerFormat.build()
                    .remark("保存订单条目")
                    .data("orderId", orderId)
                    .finish());
            orderItemDAO.saveBatch(orderItemDOList);
        }

        // 订单配送信息
        List<OrderDeliveryDetailDO> orderDeliveryDetailDOList = newOrderDataHolder.getOrderDeliveryDetailDOList();
        if (!orderDeliveryDetailDOList.isEmpty()) {
            log.info(LoggerFormat.build()
                    .remark("保存订单配送信息")
                    .data("orderId", orderId)
                    .finish());
            orderDeliveryDetailDAO.saveBatch(orderDeliveryDetailDOList);
        }

        // 订单支付信息
        List<OrderPaymentDetailDO> orderPaymentDetailDOList = newOrderDataHolder.getOrderPaymentDetailDOList();
        if (!orderPaymentDetailDOList.isEmpty()) {
            log.info(LoggerFormat.build()
                    .remark("保存订单支付信息")
                    .data("orderId", orderId)
                    .finish());
            orderPaymentDetailDAO.saveBatch(orderPaymentDetailDOList);
        }

        // 订单费用信息
        List<OrderAmountDO> orderAmountDOList = newOrderDataHolder.getOrderAmountDOList();
        if (!orderAmountDOList.isEmpty()) {
            log.info(LoggerFormat.build()
                    .remark("保存订单费用信息")
                    .data("orderId", orderId)
                    .finish());
            orderAmountDAO.saveBatch(orderAmountDOList);
        }

        // 订单费用明细
        List<OrderAmountDetailDO> orderAmountDetailDOList = newOrderDataHolder.getOrderAmountDetailDOList();
        if (!orderAmountDetailDOList.isEmpty()) {
            log.info(LoggerFormat.build()
                    .remark("保存订单费用明细")
                    .data("orderId", orderId)
                    .finish());
            orderAmountDetailDAO.saveBatch(orderAmountDetailDOList);
        }

        // 订单状态变更日志信息
        List<OrderOperateLogDO> orderOperateLogDOList = newOrderDataHolder.getOrderOperateLogDOList();
        if (!orderOperateLogDOList.isEmpty()) {
            log.info(LoggerFormat.build()
                    .remark("保存订单状态变更日志信息")
                    .data("orderId", orderId)
                    .finish());
            orderOperateLogDAO.saveBatch(orderOperateLogDOList);
        }

        // 订单快照数据
        List<OrderSnapshotDO> orderSnapshotDOList = newOrderDataHolder.getOrderSnapshotDOList();
        if (!orderSnapshotDOList.isEmpty()) {
            log.info(LoggerFormat.build()
                    .remark("保存订单快照数据")
                    .data("orderId", orderId)
                    .finish());
            orderSnapshotDAO.saveBatch(orderSnapshotDOList);
        }
    }

    /**
     * 新增主订单信息订单
     */
    private FullOrderData addNewMasterOrder(CreateOrderRequest createOrderRequest, List<ProductSkuDTO> productSkuList,
                                            CalculateOrderAmountDTO calculateOrderAmountDTO) {
        NewOrderBuilder newOrderBuilder = new NewOrderBuilder(createOrderRequest, productSkuList,
                calculateOrderAmountDTO, orderProperties, orderConverter);
        FullOrderData fullOrderData = newOrderBuilder.buildOrder()
                .buildOrderItems()
                .buildOrderDeliveryDetail()
                .buildOrderPaymentDetail()
                .buildOrderAmount()
                .buildOrderAmountDetail()
                .buildOperateLog()
                .buildOrderSnapshot()
                .build();

        // 订单信息
        OrderInfoDO orderInfoDO = fullOrderData.getOrderInfoDO();

        // 订单条目信息
        List<OrderItemDO> orderItemDOList = fullOrderData.getOrderItemDOList();

        // 订单费用信息
        List<OrderAmountDO> orderAmountDOList = fullOrderData.getOrderAmountDOList();

        // 补全地址信息
        OrderDeliveryDetailDO orderDeliveryDetailDO = fullOrderData.getOrderDeliveryDetailDO();
        String detailAddress = getDetailAddress(orderDeliveryDetailDO);
        orderDeliveryDetailDO.setDetailAddress(detailAddress);

        // 补全订单状态变更日志
        OrderOperateLogDO orderOperateLogDO = fullOrderData.getOrderOperateLogDO();
        String remark = "创建订单操作0-10";
        orderOperateLogDO.setRemark(remark);

        // 补全订单商品快照信息
        List<OrderSnapshotDO> orderSnapshotDOList = fullOrderData.getOrderSnapshotDOList();
        for (OrderSnapshotDO orderSnapshotDO : orderSnapshotDOList) {
            // 优惠券信息
            if (orderSnapshotDO.getSnapshotType().equals(SnapshotTypeEnum.ORDER_COUPON.getCode())) {
                String couponId = orderInfoDO.getCouponId();
                String userId = orderInfoDO.getUserId();
                GetUserCouponQuery userCouponQuery = new GetUserCouponQuery();
                userCouponQuery.setCouponId(couponId);
                userCouponQuery.setUserId(userId);
                UserCouponDTO userCouponDTO = marketRemote.getUserCoupon(userCouponQuery);
                if (userCouponDTO != null) {
                    orderSnapshotDO.setSnapshotJson(JSONHelper.toString(userCouponDTO));
                } else {
                    orderSnapshotDO.setSnapshotJson(JSONHelper.toString(couponId));
                }
            }
            // 订单费用信息
            else if (orderSnapshotDO.getSnapshotType().equals(SnapshotTypeEnum.ORDER_AMOUNT.getCode())) {
                orderSnapshotDO.setSnapshotJson(JSONHelper.toString(orderAmountDOList));
            }
            // 订单条目信息
            else if (orderSnapshotDO.getSnapshotType().equals(SnapshotTypeEnum.ORDER_ITEM.getCode())) {
                orderSnapshotDO.setSnapshotJson(JSONHelper.toString(orderItemDOList));
            }
        }

        return fullOrderData;
    }

    /**
     * 获取用户收货详细地址
     */
    private String getDetailAddress(OrderDeliveryDetailDO orderDeliveryDetailDO) {
        String provinceCode = orderDeliveryDetailDO.getProvince();
        String cityCode = orderDeliveryDetailDO.getCity();
        String areaCode = orderDeliveryDetailDO.getArea();
        String streetCode = orderDeliveryDetailDO.getStreet();
        AddressQuery query = new AddressQuery();
        query.setProvinceCode(provinceCode);
        query.setCityCode(cityCode);
        query.setAreaCode(areaCode);
        query.setStreetCode(streetCode);
        AddressFullDTO addressDTO = addressRemote.queryAddress(query);
        if (addressDTO == null) {
            return orderDeliveryDetailDO.getDetailAddress();
        }

        StringBuilder detailAddress = new StringBuilder();
        if (StringUtils.isNotEmpty(addressDTO.getProvince())) {
            detailAddress.append(addressDTO.getProvince());
        }
        if (StringUtils.isNotEmpty(addressDTO.getCity())) {
            detailAddress.append(addressDTO.getCity());
        }
        if (StringUtils.isNotEmpty(addressDTO.getArea())) {
            detailAddress.append(addressDTO.getArea());
        }
        if (StringUtils.isNotEmpty(addressDTO.getStreet())) {
            detailAddress.append(addressDTO.getStreet());
        }
        if (StringUtils.isNotEmpty(orderDeliveryDetailDO.getDetailAddress())) {
            detailAddress.append(orderDeliveryDetailDO.getDetailAddress());
        }
        return detailAddress.toString();
    }

    /**
     * 生成子单
     *
     * @param fullOrderData 主单数据
     * @param productType   商品类型
     */
    private FullOrderData addNewSubOrder(FullOrderData fullOrderData, Integer productType) {

        // 主单信息
        OrderInfoDO orderInfoDO = fullOrderData.getOrderInfoDO();
        // 主订单条目
        List<OrderItemDO> orderItemDOList = fullOrderData.getOrderItemDOList();
        // 主订单配送信息
        OrderDeliveryDetailDO orderDeliveryDetailDO = fullOrderData.getOrderDeliveryDetailDO();
        // 主订单支付信息
        List<OrderPaymentDetailDO> orderPaymentDetailDOList = fullOrderData.getOrderPaymentDetailDOList();
        // 主订单费用信息
        List<OrderAmountDO> orderAmountDOList = fullOrderData.getOrderAmountDOList();
        // 主订单费用明细
        List<OrderAmountDetailDO> orderAmountDetailDOList = fullOrderData.getOrderAmountDetailDOList();
        // 主订单状态变更日志信息
        OrderOperateLogDO orderOperateLogDO = fullOrderData.getOrderOperateLogDO();
        // 主订单快照数据
        List<OrderSnapshotDO> orderSnapshotDOList = fullOrderData.getOrderSnapshotDOList();


        // 父订单号
        String orderId = orderInfoDO.getOrderId();
        // 用户ID
        String userId = orderInfoDO.getUserId();

        // 生成新的子订单的订单号
        String subOrderId = orderNoManager.genOrderId(OrderNoTypeEnum.SALE_ORDER.getCode(), userId);

        // 子订单全量的数据
        FullOrderData subFullOrderData = new FullOrderData();

        // 过滤出当前商品类型的订单条目信息
        List<OrderItemDO> subOrderItemDOList = orderItemDOList.stream()
                .filter(orderItemDO -> productType.equals(orderItemDO.getProductType()))
                .collect(Collectors.toList());

        // 统计子单总金额
        BigDecimal subTotalAmount = BigDecimal.ZERO;
        BigDecimal subRealPayAmount = BigDecimal.ZERO;
        for (OrderItemDO subOrderItemDO : subOrderItemDOList) {
            subTotalAmount = subTotalAmount.add(subOrderItemDO.getOriginAmount());
            subRealPayAmount = subRealPayAmount.add(subOrderItemDO.getPayAmount());
        }

        // 订单主信息
        OrderInfoDO newSubOrderInfo = orderConverter.copyOrderInfoDTO(orderInfoDO);
        newSubOrderInfo.setId(null);
        newSubOrderInfo.setOrderId(subOrderId);
        newSubOrderInfo.setParentOrderId(orderId);
        newSubOrderInfo.setOrderStatus(OrderStatusEnum.INVALID.getCode());
        newSubOrderInfo.setTotalAmount(subTotalAmount);
        newSubOrderInfo.setPayAmount(subRealPayAmount);
        subFullOrderData.setOrderInfoDO(newSubOrderInfo);

        // 订单条目
        List<OrderItemDO> newSubOrderItemList = new ArrayList<>();
        for (OrderItemDO orderItemDO : subOrderItemDOList) {
            OrderItemDO newSubOrderItem = orderConverter.copyOrderItemDO(orderItemDO);
            newSubOrderItem.setId(null);
            newSubOrderItem.setOrderId(subOrderId);
            String subOrderItemId = getSubOrderItemId(orderItemDO.getOrderItemId(), subOrderId);
            newSubOrderItem.setOrderItemId(subOrderItemId);
            newSubOrderItemList.add(newSubOrderItem);
        }
        subFullOrderData.setOrderItemDOList(newSubOrderItemList);

        // 订单配送地址信息
        OrderDeliveryDetailDO newSubOrderDeliveryDetail = orderConverter.copyOrderDeliverDetailDO(orderDeliveryDetailDO);
        newSubOrderDeliveryDetail.setId(null);
        newSubOrderDeliveryDetail.setOrderId(subOrderId);
        subFullOrderData.setOrderDeliveryDetailDO(newSubOrderDeliveryDetail);


        Map<String, OrderItemDO> subOrderItemMap = subOrderItemDOList.stream()
                .collect(Collectors.toMap(OrderItemDO::getOrderItemId, Function.identity()));

        // 统计子订单费用信息
        BigDecimal subTotalOriginPayAmount = BigDecimal.ZERO;
        BigDecimal subTotalCouponDiscountAmount = BigDecimal.ZERO;
        BigDecimal subTotalRealPayAmount = BigDecimal.ZERO;

        // 订单费用明细
        List<OrderAmountDetailDO> subOrderAmountDetailList = new ArrayList<>();
        for (OrderAmountDetailDO orderAmountDetailDO : orderAmountDetailDOList) {
            String orderItemId = orderAmountDetailDO.getOrderItemId();
            if (!subOrderItemMap.containsKey(orderItemId)) {
                continue;
            }
            OrderAmountDetailDO subOrderAmountDetail = orderConverter.copyOrderAmountDetail(orderAmountDetailDO);
            subOrderAmountDetail.setId(null);
            subOrderAmountDetail.setOrderId(subOrderId);
            String subOrderItemId = getSubOrderItemId(orderItemId, subOrderId);
            subOrderAmountDetail.setOrderItemId(subOrderItemId);
            subOrderAmountDetailList.add(subOrderAmountDetail);

            Integer amountType = orderAmountDetailDO.getAmountType();
            BigDecimal amount = orderAmountDetailDO.getAmount();
            if (AmountTypeEnum.ORIGIN_PAY_AMOUNT.getCode().equals(amountType)) {
                subTotalOriginPayAmount = subTotalOriginPayAmount.add(amount);
            }
            if (AmountTypeEnum.COUPON_DISCOUNT_AMOUNT.getCode().equals(amountType)) {
                subTotalCouponDiscountAmount = subTotalCouponDiscountAmount.add(amount);
            }
            if (AmountTypeEnum.REAL_PAY_AMOUNT.getCode().equals(amountType)) {
                subTotalRealPayAmount = subTotalRealPayAmount.add(amount);
            }
        }
        subFullOrderData.setOrderAmountDetailDOList(subOrderAmountDetailList);

        // 订单费用信息
        List<OrderAmountDO> subOrderAmountList = new ArrayList<>();
        for (OrderAmountDO orderAmountDO : orderAmountDOList) {
            Integer amountType = orderAmountDO.getAmountType();
            OrderAmountDO subOrderAmount = orderConverter.copyOrderAmountDO(orderAmountDO);
            subOrderAmount.setId(null);
            subOrderAmount.setOrderId(subOrderId);
            if (AmountTypeEnum.ORIGIN_PAY_AMOUNT.getCode().equals(amountType)) {
                subOrderAmount.setAmount(subTotalOriginPayAmount);
                subOrderAmountList.add(subOrderAmount);
            }
            if (AmountTypeEnum.COUPON_DISCOUNT_AMOUNT.getCode().equals(amountType)) {
                subOrderAmount.setAmount(subTotalCouponDiscountAmount);
                subOrderAmountList.add(subOrderAmount);
            }
            if (AmountTypeEnum.REAL_PAY_AMOUNT.getCode().equals(amountType)) {
                subOrderAmount.setAmount(subTotalRealPayAmount);
                subOrderAmountList.add(subOrderAmount);
            }
        }
        subFullOrderData.setOrderAmountDOList(subOrderAmountList);

        // 订单支付信息
        List<OrderPaymentDetailDO> subOrderPaymentDetailDOList = new ArrayList<>();
        for (OrderPaymentDetailDO orderPaymentDetailDO : orderPaymentDetailDOList) {
            OrderPaymentDetailDO subOrderPaymentDetail = orderConverter.copyOrderPaymentDetailDO(orderPaymentDetailDO);
            subOrderPaymentDetail.setId(null);
            subOrderPaymentDetail.setOrderId(subOrderId);
            subOrderPaymentDetail.setPayAmount(subTotalRealPayAmount);
            subOrderPaymentDetailDOList.add(subOrderPaymentDetail);
        }
        subFullOrderData.setOrderPaymentDetailDOList(subOrderPaymentDetailDOList);

        // 订单状态变更日志信息
        OrderOperateLogDO subOrderOperateLogDO = orderConverter.copyOrderOperationLogDO(orderOperateLogDO);
        subOrderOperateLogDO.setId(null);
        subOrderOperateLogDO.setOrderId(subOrderId);
        subOrderOperateLogDO.setCurrentStatus(OrderStatusEnum.INVALID.getCode());
        String remark = "创建订单操作0-127";
        subOrderOperateLogDO.setRemark(remark);
        subFullOrderData.setOrderOperateLogDO(subOrderOperateLogDO);

        // 订单商品快照信息
        List<OrderSnapshotDO> subOrderSnapshotDOList = new ArrayList<>();
        for (OrderSnapshotDO orderSnapshotDO : orderSnapshotDOList) {
            OrderSnapshotDO subOrderSnapshotDO = orderConverter.copyOrderSnapshot(orderSnapshotDO);
            subOrderSnapshotDO.setId(null);
            subOrderSnapshotDO.setOrderId(subOrderId);
            if (SnapshotTypeEnum.ORDER_AMOUNT.getCode().equals(orderSnapshotDO.getSnapshotType())) {
                subOrderSnapshotDO.setSnapshotJson(JSONHelper.toString(subOrderAmountList));
            } else if (SnapshotTypeEnum.ORDER_ITEM.getCode().equals(orderSnapshotDO.getSnapshotType())) {
                subOrderSnapshotDO.setSnapshotJson(JSONHelper.toString(subOrderItemDOList));
            }
            subOrderSnapshotDOList.add(subOrderSnapshotDO);
        }
        subFullOrderData.setOrderSnapshotDOList(subOrderSnapshotDOList);
        return subFullOrderData;
    }

    /**
     * 获取子订单的orderItemId值
     */
    private String getSubOrderItemId(String orderItemId, String subOrderId) {
        String postfix = orderItemId.substring(orderItemId.indexOf("_"));
        return subOrderId + postfix;
    }

    /**
     * 支付回调更新订单状态
     *
     * @param orderInfoDO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateOrderStatusWhenPayCallback(OrderInfoDO orderInfoDO) {

        // 更新主单的状态
        updateMasterOrderStatus(orderInfoDO);

        // 判断是否存在子订单
        String orderId = orderInfoDO.getOrderId();
        List<OrderInfoDO> subOrderInfoDOList = orderInfoDAO.listByParentOrderId(orderId);
        if (subOrderInfoDOList == null || subOrderInfoDOList.isEmpty()) {
            return;
        }

        // 更新子单的状态
        updateSubOrderStatus(orderInfoDO, subOrderInfoDOList);

    }

    /**
     * 更新主订单状态
     * @param orderInfoDO
     */
    private void updateMasterOrderStatus(OrderInfoDO orderInfoDO) {

        String orderId = orderInfoDO.getOrderId();
        // 更新主单订单状态
        Integer preOrderStatus = orderInfoDO.getOrderStatus();
        Integer currentStatus = OrderStatusEnum.PAID.getCode();
        List<String> orderIdList = Collections.singletonList(orderId);
        updateOrderStatus(orderIdList, currentStatus);

        // 更新主单支付状态
        updateOrderPayStatus(orderIdList, PayStatusEnum.PAID.getCode());

        // 新增主单订单状态变更日志
        Integer operateType = OrderOperateTypeEnum.PAID_ORDER.getCode();
        String remark = "订单支付回调操作" + preOrderStatus + "-" + currentStatus;
        saveOrderOperateLog(orderId, operateType, preOrderStatus, currentStatus, remark);
    }



    /**
     * 更新子订单状态
     * @param orderInfoDO
     * @param subOrderInfoDOList
     */
    private void updateSubOrderStatus(OrderInfoDO orderInfoDO, List<OrderInfoDO> subOrderInfoDOList) {
        String orderId = orderInfoDO.getOrderId();
        Integer newPreOrderStatus = orderInfoDO.getOrderStatus();
        Integer currentOrderStatus = OrderStatusEnum.INVALID.getCode();

        // 先将主订单状态设置为无效订单
        List<String> orderIdList = Collections.singletonList(orderId);
        updateOrderStatus(orderIdList, currentOrderStatus);

        // 新增订单状态变更日志
        Integer operateType = OrderOperateTypeEnum.PAID_ORDER.getCode();
        String remark = "订单支付回调操作，主订单状态变更" + newPreOrderStatus + "-" + currentOrderStatus;
        saveOrderOperateLog(orderId, operateType, newPreOrderStatus, currentOrderStatus, remark);

        // 再更新子订单的状态
        Integer subCurrentOrderStatus = OrderStatusEnum.PAID.getCode();
        List<String> subOrderIdList = subOrderInfoDOList.stream()
                .map(OrderInfoDO::getOrderId).collect(Collectors.toList());

        // 更新子订单状态
        updateOrderStatus(subOrderIdList, subCurrentOrderStatus);

        // 更新子订单的支付明细
        updateOrderPayStatus(subOrderIdList, PayStatusEnum.PAID.getCode());

        // 保存子订单操作日志
        saveSubOrderOperateLog(subCurrentOrderStatus, subOrderInfoDOList);
    }

    /**
     * 更新订单状态
     * @param orderIdList
     * @param orderStatus
     */
    private void updateOrderStatus(List<String> orderIdList, Integer orderStatus) {
        OrderInfoDO orderInfoDO = new OrderInfoDO();
        orderInfoDO.setOrderStatus(orderStatus);
        if(orderIdList.size() == 1) {
            orderInfoDAO.updateByOrderId(orderInfoDO, orderIdList.get(0));
        } else {
            orderInfoDAO.updateBatchByOrderIds(orderInfoDO, orderIdList);
        }
    }

    /**
     * 更新订单支付状态
     * @param orderIdList
     * @param payStatus
     */
    private void updateOrderPayStatus(List<String> orderIdList, Integer payStatus) {
        OrderPaymentDetailDO orderPaymentDetailDO = new OrderPaymentDetailDO();
        orderPaymentDetailDO.setPayStatus(payStatus);
        if(orderIdList.size() == 1) {
            orderPaymentDetailDAO.updateByOrderId(orderPaymentDetailDO, orderIdList.get(0));
        } else {
            orderPaymentDetailDAO.updateBatchByOrderIds(orderPaymentDetailDO, orderIdList);
        }
    }

    /**
     * 保存订单操作日志
     * @param orderId
     * @param operateType
     * @param preOrderStatus
     * @param currentStatus
     * @param remark
     */
    private void saveOrderOperateLog(String orderId,
                                     Integer operateType,
                                     Integer preOrderStatus,
                                     Integer currentStatus,
                                     String remark) {
        OrderOperateLogDO orderOperateLogDO = new OrderOperateLogDO();
        orderOperateLogDO.setOrderId(orderId);
        orderOperateLogDO.setOperateType(operateType);
        orderOperateLogDO.setPreStatus(preOrderStatus);
        orderOperateLogDO.setCurrentStatus(currentStatus);
        orderOperateLogDO.setRemark(remark);
        orderOperateLogDAO.save(orderOperateLogDO);
    }

    /**
     * 保存子订单操作日志
     * @param subCurrentOrderStatus
     * @param subOrderInfoDOList
     */
    private void saveSubOrderOperateLog(Integer subCurrentOrderStatus, List<OrderInfoDO> subOrderInfoDOList) {
        List<OrderOperateLogDO> tempSubOrderOperateLogDOList = new ArrayList<>();
        for (OrderInfoDO subOrderInfo : subOrderInfoDOList) {
            String subOrderId = subOrderInfo.getOrderId();
            Integer subPreOrderStatus = subOrderInfo.getOrderStatus();
            // 订单状态变更日志
            OrderOperateLogDO subOrderOperateLogDO = new OrderOperateLogDO();
            subOrderOperateLogDO.setOrderId(subOrderId);
            subOrderOperateLogDO.setOperateType(OrderOperateTypeEnum.PAID_ORDER.getCode());
            subOrderOperateLogDO.setPreStatus(subPreOrderStatus);
            subOrderOperateLogDO.setCurrentStatus(subCurrentOrderStatus);
            subOrderOperateLogDO.setRemark("订单支付回调操作，子订单状态变更"
                    + subOrderOperateLogDO.getPreStatus() + "-"
                    + subOrderOperateLogDO.getCurrentStatus());
            tempSubOrderOperateLogDOList.add(subOrderOperateLogDO);
        }

        // 新增子订单状态变更日志
        if (!tempSubOrderOperateLogDOList.isEmpty()) {
            orderOperateLogDAO.saveBatch(tempSubOrderOperateLogDOList);
        }
    }


}
