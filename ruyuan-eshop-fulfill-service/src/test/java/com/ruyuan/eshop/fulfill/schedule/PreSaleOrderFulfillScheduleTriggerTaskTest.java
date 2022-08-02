package com.ruyuan.eshop.fulfill.schedule;

import com.ruyuan.eshop.fulfill.FulfillApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = FulfillApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PreSaleOrderFulfillScheduleTriggerTaskTest {

    @Autowired
    private PreSaleOrderFulfillScheduleTriggerTask preSaleOrderFulfillScheduleTriggerTask;

    @Test
    public void testTask() throws Exception {
        preSaleOrderFulfillScheduleTriggerTask.execute();
    }

}