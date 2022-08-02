package com.ruyuan.eshop.order;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.order.elasticsearch.migrate.OrderMigrateToEsHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = OrderApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MigrateToEsTest {

    @Autowired
    private OrderMigrateToEsHandler orderMigrateToEsHandler;

    @Test
    public void testOrder() throws Exception {
        JSONObject context = new JSONObject();
        context.put("offset",1);
        orderMigrateToEsHandler.execute(context.toJSONString());
    }

}
