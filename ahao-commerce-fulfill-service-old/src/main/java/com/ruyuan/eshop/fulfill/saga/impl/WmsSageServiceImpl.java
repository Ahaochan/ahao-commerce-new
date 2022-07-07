package com.ruyuan.eshop.fulfill.saga.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.fulfill.converter.FulFillConverter;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;
import com.ruyuan.eshop.fulfill.remote.WmsRemote;
import com.ruyuan.eshop.fulfill.saga.WmsSagaService;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import moe.ahao.commerce.wms.api.dto.PickDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("wmsSageService")
@Slf4j
public class WmsSageServiceImpl implements WmsSagaService {

    @Autowired
    private WmsRemote wmsRemote;

    @Autowired
    private FulFillConverter fulFillConverter;

    @Override
    public Boolean pickGoods(ReceiveFulfillRequest request) {
        log.info("捡货，request={}", JSONObject.toJSONString(request));

        //调用wms系统进行捡货
        PickDTO result = wmsRemote
                .pickGoods(buildPickGoodsRequest(request));

        log.info("捡货结果，result={}", JSONObject.toJSONString(result));

        return true;
    }

    @Override
    public Boolean pickGoodsCompensate(ReceiveFulfillRequest request) {

        log.info("补偿捡货，request={}", JSONObject.toJSONString(request));

        //调用wms系统进行捡货
        wmsRemote.cancelPickGoods(request.getOrderId());

        return true;
    }


    private PickGoodsCommand buildPickGoodsRequest(ReceiveFulfillRequest fulfillRequest) {
        PickGoodsCommand request = fulFillConverter.convertPickGoodsRequest(fulfillRequest);
        List<PickGoodsCommand.OrderItem> itemRequests = fulFillConverter.convertPickOrderItemRequest(fulfillRequest.getReceiveOrderItems());
        request.setOrderItems(itemRequests);
        return request;
    }
}
