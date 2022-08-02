package com.ruyuan.eshop.order;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.order.converter.EsEntityConverter;
import com.ruyuan.eshop.order.dao.OrderInfoDAO;
import com.ruyuan.eshop.order.domain.dto.CheckOrderStatusConsistencyResultDTO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.entity.EsOrderInfo;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import com.ruyuan.eshop.order.elasticsearch.handler.order.EsOrderListQueryIndexHandler;
import com.ruyuan.eshop.order.elasticsearch.query.OrderListQueryIndex;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = OrderApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EsClientServiceTest {

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private OrderInfoDAO orderInfoDAO;

    @Autowired
    private EsEntityConverter esEntityConverter;

    @Autowired
    private EsOrderListQueryIndexHandler esOrderListQueryIndexHandler;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testCreateIndex() throws Exception {
        boolean result = esClientService.createIndex(OrderInfoDO.class);
        System.out.println("result=" + result);
    }

    @Test
    public void index() throws Exception {
        String orderId = "1022011279374601100";
        OrderInfoDO orderInfoDO = orderInfoDAO.getByOrderId(orderId);
        EsOrderInfo esOrderInfo = esEntityConverter.convertToEsOrderInfo(orderInfoDO);
        esClientService.index(esOrderInfo);

        String result = esClientService.queryById(EsIndexNameEnum.ORDER_INFO, orderId);

        System.out.println("result =" + JSONObject.toJSONString(result));
    }

    @Test
    public void test() throws Exception {

        String str = "{\"businessIdentifier\":1,\"createdTime\":\"2022-01-11 19:40:18\",\"id\":\"1022011162523784100_001_10\",\"orderId\":\"1022011162523784100\",\"orderItemId\":\"1022011162523784100_001\",\"orderStatus\":10,\"orderType\":1,\"payAmount\":10000,\"payTime\":\"2022-01-11 19:43:30\",\"productName\":\"测试商品\",\"productType\":1,\"receiverName\":\"张三\",\"receiverPhone\":\"13434545545\",\"sellerId\":\"101\",\"skuCode\":\"skuCode001\",\"tradeNo\":\"6177156613487274183\",\"userId\":\"100\"}";

        List<OrderListQueryIndex> orderListQueryIndices = new ArrayList<>();
        OrderListQueryIndex queryIndex = JSONObject.parseObject(str, OrderListQueryIndex.class);
        orderListQueryIndices.add(queryIndex);
        esOrderListQueryIndexHandler.sycToEs(orderListQueryIndices);
    }

    @Test
    public void reIndex() throws Exception {
        esClientService.reIndex(OrderListQueryIndex.class, null);
    }

    @Test
    public void testDeleteAllIndex() throws Exception {
        for (EsIndexNameEnum esIndexName : EsIndexNameEnum.values()) {
            esClientService.deleteIndex(esIndexName);
            System.out.println("删除index=" + esIndexName.getName());
        }
    }

    @Test
    public void testCheckOrderInfoDbAndEsDataConsistency() {
        CheckOrderStatusConsistencyResultDTO checkOrderStatusConsistencyResultDTO = esClientService.checkOrderInfoDbAndEsDataConsistency(createOrderList());
        Assert.assertNotNull(checkOrderStatusConsistencyResultDTO);
    }

    private List<OrderInfoDO> createOrderList() {
        List<OrderInfoDO> orderInfoDOList = new ArrayList<>();
        OrderInfoDO orderInfo = new OrderInfoDO();
        orderInfo.setOrderId("1022011133237417100");
        orderInfo.setOrderStatus(10);

        OrderInfoDO orderInfo2 = new OrderInfoDO();
        orderInfo2.setOrderId("1022011133180072100");
        orderInfo2.setOrderStatus(10);

//        OrderInfoDO orderInfo3 = new OrderInfoDO();
//        orderInfo3.setOrderId("1022011133180071100");
//        orderInfo3.setOrderStatus(0);

        orderInfoDOList.add(orderInfo);
        orderInfoDOList.add(orderInfo2);
//        orderInfoDOList.add(orderInfo3);
        return orderInfoDOList;
    }


    @Test
    public void bulk() throws Exception {

        String id1 = "1022011454136064100";
        String id2 = "1022011466522368100";
        String map1 = esClientService.queryById(EsIndexNameEnum.ORDER_INFO,id1);
        String map2 = esClientService.queryById(EsIndexNameEnum.ORDER_INFO,id2);

        BulkRequest bulkRequest = new BulkRequest();


        IndexRequest indexRequest1 = new IndexRequest(EsIndexNameEnum.ORDER_INFO.getName());
        indexRequest1.id(id1);
        JSONObject jsonObject1 = JSONObject.parseObject(map1);
        jsonObject1.put("extJson","extJson1");
        indexRequest1.source(jsonObject1.toJSONString(), XContentType.JSON);
        indexRequest1.version(3);
        indexRequest1.versionType(VersionType.EXTERNAL);

        bulkRequest.add(indexRequest1);

        IndexRequest indexRequest2 = new IndexRequest(EsIndexNameEnum.ORDER_INFO.getName());
        indexRequest2.id(id2);
        JSONObject jsonObject2 = JSONObject.parseObject(map2);
        jsonObject1.put("extJson","extJson2");
        indexRequest2.source(jsonObject2.toJSONString(), XContentType.JSON);
        indexRequest2.version(7);
        indexRequest2.versionType(VersionType.EXTERNAL);

        bulkRequest.add(indexRequest2);


        BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);



        System.out.println(JSONObject.toJSONString(response));
        System.out.println(response.hasFailures());
        System.out.println(response.buildFailureMessage());
    }
}