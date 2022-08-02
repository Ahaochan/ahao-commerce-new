package com.ruyuan.eshop.fulfill.service;

import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.request.TriggerOrderAfterFulfillEventRequest;

/**
 * 订单履约后事件处理器
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public interface OrderAfterFulfillEventProcessor {

    /**
     * 执行
     *
     * @param request
     */
    void execute(TriggerOrderAfterFulfillEventRequest request, OrderFulfillDO orderFulfill);

}
