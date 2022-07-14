package com.ruyuan.eshop.order.remote;

import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import moe.ahao.commerce.fulfill.api.FulfillFeignApi;
import moe.ahao.commerce.fulfill.api.command.CancelFulfillCommand;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 履约服务远程接口
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class FulfillRemote {

    @Autowired
    private FulfillFeignApi fulfillApi;

    /**
     * 取消订单履约
     * @param cancelFulfillRequest
     */
    public void cancelFulfill(CancelFulfillCommand cancelFulfillRequest) {
        Result<Boolean> result = fulfillApi.cancelFulfill(cancelFulfillRequest);
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderBizException(OrderErrorCodeEnum.CANCEL_ORDER_FULFILL_ERROR);
        }
    }
}
