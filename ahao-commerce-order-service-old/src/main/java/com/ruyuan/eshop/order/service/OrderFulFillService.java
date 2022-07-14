package com.ruyuan.eshop.order.service;


import com.ruyuan.eshop.order.domain.dto.WmsShipDTO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.exception.OrderBizException;
import moe.ahao.commerce.fulfill.api.command.ReceiveFulfillCommand;

/**
 * <p>
 * 订单履约相关service
 * </p>
 *
 * @author zhonghuashishan
 */
public interface OrderFulFillService {

    /**
     * 触发订单进行履约流程
     *
     * @param orderId
     * @return
     */
    void triggerOrderFulFill(String orderId) throws OrderBizException;

    /**
     * 通知订单物流配送结果接口
     *
     * @return
     */
    void informOrderWmsShipResult(WmsShipDTO wmsShipDTO) throws OrderBizException;


    ReceiveFulfillCommand buildReceiveFulFillRequest(OrderInfoDO orderInfo);
}
