package com.ruyuan.eshop.order.statemachine.action.order.create.node;

import com.ruyuan.eshop.common.enums.AmountTypeEnum;
import com.ruyuan.eshop.common.utils.LoggerFormat;
import com.ruyuan.eshop.market.domain.dto.CalculateOrderAmountDTO;
import com.ruyuan.eshop.market.domain.request.CalculateOrderAmountRequest;
import com.ruyuan.eshop.order.converter.OrderConverter;
import com.ruyuan.eshop.order.domain.dto.OrderAmountDTO;
import com.ruyuan.eshop.order.domain.dto.OrderAmountDetailDTO;
import com.ruyuan.eshop.order.domain.request.CreateOrderRequest;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.remote.MarketRemote;
import com.ruyuan.eshop.order.remote.ProductRemote;
import com.ruyuan.eshop.product.domain.dto.ProductSkuDTO;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 创建订单计算金额节点
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class CreateOrderCalculateAmountNode extends StandardProcessor {

    @Autowired
    private ProductRemote productRemote;

    @Autowired
    private OrderConverter orderConverter;

    @Autowired
    private MarketRemote marketRemote;

    @Override
    protected void processInternal(ProcessContext processContext) {
        CreateOrderRequest createOrderRequest = processContext.get("createOrderRequest");
        // 3、获取商品信息
        List<ProductSkuDTO> productSkuList = listProductSkus(createOrderRequest);

        // 4、计算订单价格
        CalculateOrderAmountDTO calculateOrderAmountDTO = calculateOrderAmount(createOrderRequest, productSkuList);

        // 5、验证订单实付金额
        checkRealPayAmount(createOrderRequest, calculateOrderAmountDTO);

        // 设置参数，传递给后面节点
        processContext.set("productSkuList", productSkuList);
        processContext.set("calculateOrderAmountDTO", calculateOrderAmountDTO);
    }

    /**
     * 获取订单条目商品信息
     */
    private List<ProductSkuDTO> listProductSkus(CreateOrderRequest createOrderRequest) {
        List<CreateOrderRequest.OrderItemRequest> orderItemRequestList = createOrderRequest.getOrderItemRequestList();

        List<String> skuCodeList = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest orderItemRequest : orderItemRequestList) {
            String skuCode = orderItemRequest.getSkuCode();
            skuCodeList.add(skuCode);
        }
        List<ProductSkuDTO> productSkuList = productRemote.listProductSku(skuCodeList, createOrderRequest.getSellerId());
        log.info(LoggerFormat.build()
                .remark("listProductSkus->return")
                .data("productSkus", productSkuList)
                .finish());
        return productSkuList;
    }

    /**
     * 计算订单价格
     * 如果使用了优惠券、红包、积分等，会一并进行扣减
     *
     * @param createOrderRequest 订单信息
     * @param productSkuList     商品信息
     */
    private CalculateOrderAmountDTO calculateOrderAmount(CreateOrderRequest createOrderRequest, List<ProductSkuDTO> productSkuList) {

        CalculateOrderAmountRequest calculateOrderPriceRequest = orderConverter.convertToCalculateOrderAmountRequest(createOrderRequest);

        // 订单条目补充商品信息
        Map<String, ProductSkuDTO> productSkuDTOMap = productSkuList.stream().collect(Collectors.toMap(ProductSkuDTO::getSkuCode, Function.identity()));
        calculateOrderPriceRequest.getOrderItemRequestList().forEach(item -> {
            String skuCode = item.getSkuCode();
            ProductSkuDTO productSkuDTO = productSkuDTOMap.get(skuCode);
            item.setProductId(productSkuDTO.getProductId());
            item.setSalePrice(productSkuDTO.getSalePrice());
        });

        // 调用营销服务计算订单价格
        CalculateOrderAmountDTO calculateOrderAmountDTO = marketRemote.calculateOrderAmount(calculateOrderPriceRequest);
        if (calculateOrderAmountDTO == null) {
            throw new OrderBizException(OrderErrorCodeEnum.CALCULATE_ORDER_AMOUNT_ERROR);
        }
        // 订单费用信息
        List<OrderAmountDTO> orderAmountList = orderConverter.convertOrderAmountDTO(calculateOrderAmountDTO.getOrderAmountList());
        if (orderAmountList == null || orderAmountList.isEmpty()) {
            throw new OrderBizException(OrderErrorCodeEnum.CALCULATE_ORDER_AMOUNT_ERROR);
        }

        // 订单条目费用明细
        List<OrderAmountDetailDTO> orderItemAmountList = orderConverter.convertOrderAmountDetail(calculateOrderAmountDTO.getOrderAmountDetail());
        if (orderItemAmountList == null || orderItemAmountList.isEmpty()) {
            throw new OrderBizException(OrderErrorCodeEnum.CALCULATE_ORDER_AMOUNT_ERROR);
        }
        log.info(LoggerFormat.build()
                .remark("calculateOrderAmount->return")
                .data("return", calculateOrderAmountDTO)
                .finish());
        return calculateOrderAmountDTO;
    }

    /**
     * 验证订单实付金额
     */
    private void checkRealPayAmount(CreateOrderRequest createOrderRequest, CalculateOrderAmountDTO calculateOrderAmountDTO) {
        List<CreateOrderRequest.OrderAmountRequest> originOrderAmountRequestList = createOrderRequest.getOrderAmountRequestList();
        Map<Integer, CreateOrderRequest.OrderAmountRequest> originOrderAmountMap =
                originOrderAmountRequestList.stream().collect(Collectors.toMap(
                        CreateOrderRequest.OrderAmountRequest::getAmountType, Function.identity()));
        // 前端给的实付金额
        Integer originRealPayAmount = originOrderAmountMap.get(AmountTypeEnum.REAL_PAY_AMOUNT.getCode()).getAmount();


        List<CalculateOrderAmountDTO.OrderAmountDTO> orderAmountDTOList = calculateOrderAmountDTO.getOrderAmountList();
        Map<Integer, CalculateOrderAmountDTO.OrderAmountDTO> orderAmountMap =
                orderAmountDTOList.stream().collect(Collectors.toMap(CalculateOrderAmountDTO.OrderAmountDTO::getAmountType, Function.identity()));
        // 营销计算出来的实付金额
        Integer realPayAmount = orderAmountMap.get(AmountTypeEnum.REAL_PAY_AMOUNT.getCode()).getAmount();

        if (!originRealPayAmount.equals(realPayAmount)) {
            // 订单验价失败
            throw new OrderBizException(OrderErrorCodeEnum.ORDER_CHECK_REAL_PAY_AMOUNT_FAIL);
        }
    }
}
