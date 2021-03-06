package moe.ahao.commerce.order.application;


import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.infrastructure.enums.OrderCancelTypeEnum;
import moe.ahao.commerce.common.constants.RocketMqConstant;
import moe.ahao.commerce.common.enums.AmountTypeEnum;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.common.enums.PayTypeEnum;
import moe.ahao.commerce.common.infrastructure.event.PayOrderTimeoutEvent;
import moe.ahao.commerce.common.infrastructure.rocketmq.RocketDelayedLevel;
import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.market.api.query.CalculateOrderAmountQuery;
import moe.ahao.commerce.order.api.command.CreateOrderCommand;
import moe.ahao.commerce.order.api.dto.CreateOrderDTO;
import moe.ahao.commerce.order.infrastructure.enums.AccountTypeEnum;
import moe.ahao.commerce.order.infrastructure.enums.BusinessIdentifierEnum;
import moe.ahao.commerce.order.infrastructure.enums.DeliveryTypeEnum;
import moe.ahao.commerce.order.infrastructure.enums.OrderTypeEnum;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.gateway.MarketCalculateGateway;
import moe.ahao.commerce.order.infrastructure.gateway.ProductGateway;
import moe.ahao.commerce.order.infrastructure.gateway.RiskGateway;
import moe.ahao.commerce.order.infrastructure.publisher.DefaultProducer;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.ListProductSkuQuery;
import moe.ahao.commerce.risk.api.command.CheckOrderRiskCommand;
import moe.ahao.exception.CommonBizExceptionEnum;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CreateOrderAppService {
    @Autowired
    private CreateOrderTxService createOrderTxService;

    @Autowired
    private ProductGateway productGateway;
    @Autowired
    private MarketCalculateGateway marketCalculateGateway;
    @Autowired
    private RiskGateway riskGateway;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private DefaultProducer defaultProducer;

    public CreateOrderDTO createOrder(CreateOrderCommand command) {
        log.info("????????????command:{}", command);

        // 1. ????????????
        this.checkCommand(command);
        String orderId = command.getOrderId();

        // 2. ????????????
        this.checkRisk(command);

        // 3. ??????????????????
        List<ProductSkuDTO> productList = this.listProduct(command);

        // 4. ??????????????????
        CalculateOrderAmountDTO calculateOrderAmountDTO = this.calculateOrderAmount(command, productList);

        // 5. ????????????????????????
        this.checkRealPayAmount(command, calculateOrderAmountDTO);

        // 6. ???????????????????????????????????????????????????????????????
        createOrderTxService.addNewOrder(command, productList, calculateOrderAmountDTO);

        // 7. ??????????????????????????????????????????????????????
        sendPayOrderTimeoutDelayMessage(command);

        // ??????????????????
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setOrderId(orderId);
        return dto;
    }

    private void checkCommand(CreateOrderCommand command) {
        if (command == null) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // ????????????
        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw OrderExceptionEnum.ORDER_ID_IS_NULL.msg();
        }
        OrderInfoDO orderInfo = orderInfoMapper.selectOneByOrderId(orderId);
        if (orderInfo != null) {
            throw OrderExceptionEnum.ORDER_EXISTED.msg();
        }

        // ???????????????
        Integer businessIdentifier = command.getBusinessIdentifier();
        if(businessIdentifier == null) {
            throw OrderExceptionEnum.BUSINESS_IDENTIFIER_IS_NULL.msg();
        }
        BusinessIdentifierEnum businessIdentifierEnum = BusinessIdentifierEnum.getByCode(businessIdentifier);
        if(businessIdentifierEnum == null) {
            throw OrderExceptionEnum.BUSINESS_IDENTIFIER_ERROR.msg();
        }

        // ??????ID
        String userId = command.getUserId();
        if(StringUtils.isEmpty(userId)) {
            throw OrderExceptionEnum.USER_ID_IS_NULL.msg();
        }

        // ????????????
        Integer orderType = command.getOrderType();
        if(orderType == null) {
            throw OrderExceptionEnum.ORDER_TYPE_IS_NULL.msg();
        }
        OrderTypeEnum orderTypeEnum = OrderTypeEnum.getByCode(orderType);
        if (OrderTypeEnum.UNKNOWN == orderTypeEnum) {
            throw OrderExceptionEnum.ORDER_TYPE_ERROR.msg();
        }

        // ??????ID
        String sellerId = command.getSellerId();
        if(StringUtils.isEmpty(sellerId)) {
            throw OrderExceptionEnum.SELLER_ID_IS_NULL.msg();
        }

        // ????????????
        Integer deliveryType = command.getDeliveryType();
        if(deliveryType == null) {
            throw OrderExceptionEnum.DELIVERY_TYPE_IS_NULL.msg();
        }
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.getByCode(deliveryType);
        if (deliveryTypeEnum == null) {
            throw OrderExceptionEnum.DELIVERY_TYPE_ERROR.msg();
        }

        // ????????????
        String province = command.getProvince();
        String city = command.getCity();
        String area = command.getArea();
        String street = command.getStreet();
        if(StringUtils.isAnyEmpty(province, city, area, street)) {
            throw OrderExceptionEnum.USER_ADDRESS_ERROR.msg();
        }

        // ??????ID
        String regionId = command.getRegionId();
        if(StringUtils.isEmpty(regionId)) {
            throw OrderExceptionEnum.REGION_ID_IS_NULL.msg();
        }

        // ?????????
        BigDecimal lon = command.getLon();
        BigDecimal lat = command.getLat();
        if(lon == null || lat == null) {
            throw OrderExceptionEnum.USER_LOCATION_IS_NULL.msg();
        }

        // ???????????????
        String receiverName = command.getReceiverName();
        String receiverPhone = command.getReceiverPhone();
        if(StringUtils.isAnyEmpty(receiverName, receiverPhone)) {
            throw OrderExceptionEnum.ORDER_RECEIVER_IS_NULL.msg();
        }

        // ?????????????????????
        String clientIp = command.getClientIp();
        if(StringUtils.isEmpty(clientIp)) {
            throw OrderExceptionEnum.CLIENT_IP_IS_NULL.msg();
        }

        // ??????????????????
        List<CreateOrderCommand.OrderItem> orderItems = command.getOrderItems();
        if(CollectionUtils.isEmpty(orderItems)) {
            throw OrderExceptionEnum.ORDER_ITEM_IS_NULL.msg();
        }
        for (CreateOrderCommand.OrderItem orderItem : orderItems) {
            Integer productType = orderItem.getProductType();
            BigDecimal saleQuantity = orderItem.getSaleQuantity();
            String skuCode = orderItem.getSkuCode();
            if(productType == null || saleQuantity == null || StringUtils.isEmpty(skuCode)) {
                throw OrderExceptionEnum.ORDER_ITEM_PARAM_ERROR.msg();
            }
        }

        // ??????????????????
        List<CreateOrderCommand.OrderAmount> orderAmounts = command.getOrderAmounts();
        if(CollectionUtils.isEmpty(orderAmounts)) {
            throw OrderExceptionEnum.ORDER_AMOUNT_IS_NULL.msg();
        }
        Map<AmountTypeEnum, BigDecimal> orderAmountMap = new HashMap<>();
        for (CreateOrderCommand.OrderAmount orderAmount : orderAmounts) {
            Integer amountType = orderAmount.getAmountType();
            BigDecimal amount = orderAmount.getAmount();
            if(amountType == null) {
                throw OrderExceptionEnum.ORDER_AMOUNT_TYPE_IS_NULL.msg();
            }
            AmountTypeEnum amountTypeEnum = AmountTypeEnum.getByCode(amountType);
            if (amountTypeEnum == null) {
                throw OrderExceptionEnum.ORDER_AMOUNT_TYPE_PARAM_ERROR.msg();
            }
            orderAmountMap.put(amountTypeEnum, amount);
        }
        // ??????????????????????????????
        if (orderAmountMap.get(AmountTypeEnum.ORIGIN_PAY_AMOUNT) == null) {
            throw OrderExceptionEnum.ORDER_ORIGIN_PAY_AMOUNT_IS_NULL.msg();
        }
        // ????????????????????????
        if (orderAmountMap.get(AmountTypeEnum.SHIPPING_AMOUNT) == null) {
            throw OrderExceptionEnum.ORDER_SHIPPING_AMOUNT_IS_NULL.msg();
        }
        // ??????????????????????????????
        if (orderAmountMap.get(AmountTypeEnum.REAL_PAY_AMOUNT) == null) {
            throw OrderExceptionEnum.ORDER_REAL_PAY_AMOUNT_IS_NULL.msg();
        }

        String couponId = command.getCouponId();
        BigDecimal couponDiscountAmount = orderAmountMap.get(AmountTypeEnum.COUPON_DISCOUNT_AMOUNT);
        // ???????????????????????????????????????
        if (StringUtils.isNotEmpty(couponId) && couponDiscountAmount == null) {
            throw OrderExceptionEnum.ORDER_DISCOUNT_AMOUNT_IS_NULL.msg();
        }

        // ??????????????????
        List<CreateOrderCommand.OrderPayment> orderPayments = command.getOrderPayments();
        if(CollectionUtils.isEmpty(orderPayments)) {
            throw OrderExceptionEnum.ORDER_PAYMENT_IS_NULL.msg();
        }
        for (CreateOrderCommand.OrderPayment orderPayment : orderPayments) {
            Integer payType = orderPayment.getPayType();
            PayTypeEnum payTypeEnum = PayTypeEnum.getByCode(payType);
            if (payTypeEnum == null) {
                throw OrderExceptionEnum.PAY_TYPE_PARAM_ERROR.msg();
            }
            Integer accountType = orderPayment.getAccountType();
            AccountTypeEnum accountTypeEnum = AccountTypeEnum.getByCode(accountType);
            if (accountTypeEnum == null) {
                throw OrderExceptionEnum.ORDER_AMOUNT_TYPE_PARAM_ERROR.msg();
            }
        }
    }

    private void checkRisk(CreateOrderCommand createOrderCommand) {
        // ????????????????????????????????????
        CheckOrderRiskCommand command = new CheckOrderRiskCommand();
        command.setBusinessIdentifier(createOrderCommand.getBusinessIdentifier());
        command.setOrderId(createOrderCommand.getOrderId());
        command.setUserId(createOrderCommand.getUserId());
        command.setSellerId(createOrderCommand.getSellerId());
        command.setClientIp(createOrderCommand.getClientIp());
        command.setDeviceId(createOrderCommand.getDeviceId());

        riskGateway.checkOrderRisk(command);
    }

    private List<ProductSkuDTO> listProduct(CreateOrderCommand command) {
        String sellerId = command.getSellerId();
        List<String> skuCodeList = command.getOrderItems().stream()
            .map(CreateOrderCommand.OrderItem::getSkuCode)
            .collect(Collectors.toList());

        ListProductSkuQuery query = new ListProductSkuQuery();
        query.setSellerId(sellerId);
        query.setSkuCodeList(skuCodeList);
        List<ProductSkuDTO> productSkuList = productGateway.listBySkuCodes(query);
        return productSkuList;
    }

    private CalculateOrderAmountDTO calculateOrderAmount(CreateOrderCommand command, List<ProductSkuDTO> productSkuList) {
        // 1. ??????????????????????????????
        Map<String, ProductSkuDTO> productSkuDTOMap = productSkuList.stream()
            .collect(Collectors.toMap(ProductSkuDTO::getSkuCode, Function.identity()));

        // 2. ???????????????????????????????????????
        CalculateOrderAmountQuery query = new CalculateOrderAmountQuery();
        query.setOrderId(command.getOrderId());
        query.setUserId(command.getUserId());
        query.setSellerId(command.getSellerId());
        query.setCouponId(command.getCouponId());
        query.setRegionId(command.getRegionId());
        List<CalculateOrderAmountQuery.OrderItem> orderItemQueryList = new ArrayList<>();
        for (CreateOrderCommand.OrderItem orderItemCommand : command.getOrderItems()) {
            ProductSkuDTO productSkuDTO = productSkuDTOMap.get(orderItemCommand.getSkuCode());

            CalculateOrderAmountQuery.OrderItem orderItemAmountQuery = new CalculateOrderAmountQuery.OrderItem();
            orderItemAmountQuery.setProductId(productSkuDTO.getProductId());
            orderItemAmountQuery.setSkuCode(orderItemCommand.getSkuCode());
            orderItemAmountQuery.setSalePrice(productSkuDTO.getSalePrice());
            orderItemAmountQuery.setSaleQuantity(orderItemCommand.getSaleQuantity());

            orderItemQueryList.add(orderItemAmountQuery);
        }
        query.setOrderItemList(orderItemQueryList);

        List<CalculateOrderAmountQuery.OrderAmount> orderAmountQueryList = new ArrayList<>();
        for (CreateOrderCommand.OrderAmount orderAmountCommand : command.getOrderAmounts()) {

            CalculateOrderAmountQuery.OrderAmount orderAmountQuery = new CalculateOrderAmountQuery.OrderAmount();
            orderAmountQuery.setAmountType(orderAmountCommand.getAmountType());
            orderAmountQuery.setAmount(orderAmountCommand.getAmount());

            orderAmountQueryList.add(orderAmountQuery);
        }
        query.setOrderAmountList(orderAmountQueryList);

        // 3. ????????????????????????????????????
        CalculateOrderAmountDTO calculateOrderAmountDTO = marketCalculateGateway.calculateOrderAmount(query);
        if (calculateOrderAmountDTO == null) {
            throw OrderExceptionEnum.CALCULATE_ORDER_AMOUNT_ERROR.msg();
        }
        return calculateOrderAmountDTO;
    }

    private void checkRealPayAmount(CreateOrderCommand command, CalculateOrderAmountDTO calculateDTO) {
        // ????????????????????????
        Map<Integer, CreateOrderCommand.OrderAmount> originOrderAmountMap = command.getOrderAmounts().stream()
            .collect(Collectors.toMap(
                CreateOrderCommand.OrderAmount::getAmountType, Function.identity()));

        BigDecimal originRealPayAmount = originOrderAmountMap.get(AmountTypeEnum.REAL_PAY_AMOUNT.getCode()).getAmount();

        // ?????????????????????????????????
        Map<Integer, CalculateOrderAmountDTO.OrderAmountDTO> orderAmountMap = calculateDTO.getOrderAmountList().stream()
            .collect(Collectors.toMap(CalculateOrderAmountDTO.OrderAmountDTO::getAmountType, Function.identity()));
        BigDecimal realPayAmount = orderAmountMap.get(AmountTypeEnum.REAL_PAY_AMOUNT.getCode()).getAmount();

        // ??????????????????
        if (originRealPayAmount.compareTo(realPayAmount) != 0) {
            throw OrderExceptionEnum.ORDER_CHECK_REAL_PAY_AMOUNT_FAIL.msg();
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     */
    private void sendPayOrderTimeoutDelayMessage(CreateOrderCommand command) {
        PayOrderTimeoutEvent event = new PayOrderTimeoutEvent();

        String orderId = command.getOrderId();
        event.setOrderId(orderId);
        event.setBusinessIdentifier(command.getBusinessIdentifier());
        event.setCancelType(OrderCancelTypeEnum.TIMEOUT_CANCELED.getCode());
        event.setUserId(command.getUserId());
        event.setOrderType(command.getOrderType());
        event.setOrderStatus(OrderStatusEnum.CREATED.getCode());

        String msgJson = JSONHelper.toString(event);
        defaultProducer.sendMessage(RocketMqConstant.PAY_ORDER_TIMEOUT_DELAY_TOPIC, msgJson,
            RocketDelayedLevel.DELAYED_30m, "??????????????????????????????", null, orderId);
    }
}
