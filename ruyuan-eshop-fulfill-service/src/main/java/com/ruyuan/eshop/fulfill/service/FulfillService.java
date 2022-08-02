package com.ruyuan.eshop.fulfill.service;

import com.ruyuan.eshop.fulfill.builder.FulfillData;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;

/**
 * 履约service
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public interface FulfillService {

    /**
     * 创建履约单
     *
     * @param request
     */
    FulfillData createFulfillOrders(ReceiveFulfillRequest request);

    /**
     * 取消履约单
     *
     * @param orderId
     */
    @Deprecated
    void cancelFulfillOrder(String orderId);

    /**
     * 触发履约
     */
    Boolean receiveOrderFulFill(ReceiveFulfillRequest request);

}
