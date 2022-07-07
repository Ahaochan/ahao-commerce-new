package com.ruyuan.eshop.order.manager;

import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.domain.request.CreateOrderRequest;
import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.product.api.dto.ProductSkuDTO;

import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
public interface OrderManager {


    /**
     * 生成订单
     *
     * @param createOrderRequest
     * @param productSkuList
     * @param calculateOrderAmountDTO
     */
    void createOrder(CreateOrderRequest createOrderRequest, List<ProductSkuDTO> productSkuList, CalculateOrderAmountDTO calculateOrderAmountDTO);

    /**
     * 支付回调更新订单状态
     *
     * @param orderInfoDO
     */
    void updateOrderStatusWhenPayCallback(OrderInfoDO orderInfoDO);

}
