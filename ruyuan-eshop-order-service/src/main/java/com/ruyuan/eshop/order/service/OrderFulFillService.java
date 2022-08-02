package com.ruyuan.eshop.order.service;


import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;
import com.ruyuan.eshop.order.domain.dto.AfterFulfillDTO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.exception.OrderBizException;

/**
 * <p>
 * 订单履约相关service
 * </p>
 *
 * @author zhonghuashishan
 */
public interface OrderFulFillService {

    /**
     * 将订单已履约
     *
     * @param orderId 订单id
     */
    void triggerOrderFulFill(String orderId) throws OrderBizException;

    /**
     * 通知订单物流配送结果接口
     */
    void informOrderAfterFulfillResult(AfterFulfillDTO afterFulfillDTO) throws OrderBizException;


    ReceiveFulfillRequest buildReceiveFulFillRequest(OrderInfoDO orderInfo);
}
