package com.ruyuan.eshop.fulfill.saga.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.fulfill.converter.FulFillConverter;
import com.ruyuan.eshop.fulfill.dao.OrderFulfillDAO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;
import com.ruyuan.eshop.fulfill.remote.TmsRemote;
import com.ruyuan.eshop.fulfill.saga.TmsSagaService;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.tms.api.command.SendOutCommand;
import moe.ahao.commerce.tms.api.dto.SendOutDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Service("tmsSagaService")
@Slf4j
public class TmsSagaServiceImpl implements TmsSagaService {

    @Autowired
    private TmsRemote tmsRemote;

    @Autowired
    private OrderFulfillDAO orderFulfillDAO;

    @Autowired
    private FulFillConverter fulFillConverter;

    @Override
    public Boolean sendOut(ReceiveFulfillRequest request) {

        log.info("发货，request={}", JSONObject.toJSONString(request));

        //1、调用tms进行发货
        SendOutDTO result = tmsRemote.sendOut(buildSendOutRequest(request));
        log.info("发货结果，result={}", JSONObject.toJSONString(result));

        //2、查询履约单
        OrderFulfillDO orderFulfill = orderFulfillDAO.getOne(request.getOrderId());

        //3、存储物流单号
        String logisticsCode = result.getLogisticsCode();
        orderFulfillDAO.saveLogisticsCode(orderFulfill.getFulfillId(), logisticsCode);

        return true;
    }

    @Override
    public Boolean sendOutCompensate(ReceiveFulfillRequest request) {
        log.info("补偿发货，request={}", JSONObject.toJSONString(request));

        //调用tms进行补偿发货
        tmsRemote.cancelSendOut(request.getOrderId());

        return true;
    }

    private SendOutCommand buildSendOutRequest(ReceiveFulfillRequest fulfillRequest) {
        SendOutCommand request = fulFillConverter.convertReceiveFulfillRequest(fulfillRequest);
        List<SendOutCommand.OrderItem> itemRequests = fulFillConverter.convertSendOutOrderItemRequest(fulfillRequest.getReceiveOrderItems());
        request.setOrderItems(itemRequests);
        return request;
    }
}
