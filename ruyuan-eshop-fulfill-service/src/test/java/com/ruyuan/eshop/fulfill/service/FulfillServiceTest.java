package com.ruyuan.eshop.fulfill.service;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.fulfill.FulfillApplication;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = FulfillApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FulfillServiceTest {

    @Autowired
    private FulfillService fulfillService;

    @Test
    public void test() throws Exception {
        String request = "{\"businessIdentifier\":1,\"deliveryAmount\":0,\"deliveryType\":1,\"orderId\":\"1022010466464057100\",\"orderType\":2,\"payAmount\":10000,\"payType\":10,\"receiveOrderItems\":[{\"originAmount\":10000,\"payAmount\":10000,\"productName\":\"虚拟商品\",\"productType\":2,\"productUnit\":\"个\",\"salePrice\":1000,\"saleQuantity\":10,\"skuCode\":\"skuCode003\"}],\"receiverArea\":\"110105\",\"receiverCity\":\"110100\",\"receiverDetailAddress\":\"北京北京市东城区朝阳门街道北京路10号\",\"receiverLat\":1010.2010100000,\"receiverLon\":100.1000000000,\"receiverName\":\"张三\",\"receiverPhone\":\"13434545545\",\"receiverProvince\":\"110000\",\"receiverStreet\":\"110101007\",\"sellerId\":\"101\",\"totalAmount\":10000,\"userId\":\"100\"}";

        ReceiveFulfillRequest receiveFulfillRequest = JSONObject.parseObject(request, ReceiveFulfillRequest.class);

        fulfillService.receiveOrderFulFill(receiveFulfillRequest);
    }

}