package com.ruyuan.eshop.fulfill.api.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.fulfill.api.FulfillApi;
import com.ruyuan.eshop.fulfill.converter.FulFillConverter;
import com.ruyuan.eshop.fulfill.dao.OrderFulfillDAO;
import com.ruyuan.eshop.fulfill.dao.OrderFulfillLogDAO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillLogDO;
import com.ruyuan.eshop.fulfill.domain.request.CancelFulfillRequest;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;
import com.ruyuan.eshop.fulfill.domain.request.TriggerOrderAfterFulfillEventRequest;
import com.ruyuan.eshop.fulfill.dto.OrderFulfillDTO;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillOperateTypeEnum;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillStatusEnum;
import com.ruyuan.eshop.fulfill.exception.FulfillBizException;
import com.ruyuan.eshop.fulfill.service.FulfillService;
import com.ruyuan.eshop.fulfill.service.OrderAfterFulfillEventProcessor;
import com.ruyuan.eshop.fulfill.service.impl.OrderFulfillOperateLogFactory;
import com.ruyuan.eshop.fulfill.service.impl.WmsShipEventProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@DubboService(version = "1.0.0", interfaceClass = FulfillApi.class, retries = 0)
public class FulfillApiImpl implements FulfillApi {


    @Autowired
    private FulfillService fulfillService;

    @Autowired
    private OrderFulfillDAO orderFulfillDAO;

    @Autowired
    private OrderFulfillLogDAO orderFulfillLogDAO;

    @Autowired
    private OrderFulfillOperateLogFactory orderFulfillOperateLogFactory;

    @Autowired
    private WmsShipEventProcessorFactory wmsShipEventProcessorFactory;

    @Autowired
    private FulFillConverter fulFillConverter;

    @Override
    public JsonResult<Boolean> receiveOrderFulFill(ReceiveFulfillRequest request) {
        try {
            Boolean result = fulfillService.receiveOrderFulFill(request);
            return JsonResult.buildSuccess(result);
        } catch (FulfillBizException e) {
            log.error("biz error", e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error", e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<Boolean> triggerOrderWmsShipEvent(TriggerOrderAfterFulfillEventRequest request) {
        log.info("触发订单物流配送结果事件，request={}", JSONObject.toJSONString(request));

        // 1、获取履约单
        OrderFulfillDO orderFulfill = orderFulfillDAO.getOne(request.getFulfillId());

        // 2、获取处理器
        OrderStatusChangeEnum orderStatusChange = request.getOrderStatusChange();
        OrderAfterFulfillEventProcessor processor = wmsShipEventProcessorFactory.getWmsShipEventProcessor(orderStatusChange);


        //  3、执行
        if (null != processor) {
            processor.execute(request, orderFulfill);
        }

        return JsonResult.buildSuccess(true);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonResult<Boolean> cancelFulfill(CancelFulfillRequest cancelFulfillRequest) {
        log.info("取消履约：request={}", JSONObject.toJSONString(cancelFulfillRequest));
        String orderId = cancelFulfillRequest.getOrderId();

        // 1、查询对应的履约单
        List<OrderFulfillDO> orderFulfills = orderFulfillDAO.listByOrderId(orderId);
        if (CollectionUtils.isEmpty(orderFulfills)) {
            return JsonResult.buildSuccess(true);
        }

        // 2、判断履约单是否可以履约
        for (OrderFulfillDO orderFulfill : orderFulfills) {
            if (OrderFulfillStatusEnum.notCancelStatus().contains(orderFulfill.getStatus())) {
                log.info("订单无法取消履约，存在履约单已出库配送了：orderId={}", orderId);
                return JsonResult.buildSuccess(false);
            }
        }

        // 3、取消履约
        List<String> fulfillIds = orderFulfills.stream().map(OrderFulfillDO::getFulfillId).collect(Collectors.toList());
        orderFulfillDAO.batchUpdateStatus(fulfillIds, OrderFulfillStatusEnum.FULFILL.getCode()
                , OrderFulfillStatusEnum.CANCELLED.getCode());

        // 4、添加履约单变更记录
        List<OrderFulfillLogDO> logs = new ArrayList<>(orderFulfills.size());
        orderFulfills.forEach(orderFulfill -> logs.add(orderFulfillOperateLogFactory.get(orderFulfill,
                OrderFulfillOperateTypeEnum.CANCEL_ORDER)));
        orderFulfillLogDAO.saveBatch(logs);

        return JsonResult.buildSuccess(true);
    }

    @Override
    public JsonResult<List<OrderFulfillDTO>> listOrderFulfills(String orderId) {

        // 查询对应的履约单
        List<OrderFulfillDO> orderFulfills = orderFulfillDAO.listByOrderId(orderId);

        // 转换
        return JsonResult.buildSuccess(fulFillConverter.convertToOrderFulfillDTOs(orderFulfills));
    }
}
