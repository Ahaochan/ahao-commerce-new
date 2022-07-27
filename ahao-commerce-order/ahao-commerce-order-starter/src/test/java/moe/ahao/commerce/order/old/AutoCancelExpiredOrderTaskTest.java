package moe.ahao.commerce.order.old;

import moe.ahao.commerce.aftersale.adapter.schedule.AutoCancelExpiredOrderTask;
import moe.ahao.commerce.order.OrderApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = OrderApplication.class)
@ActiveProfiles("test")
public class AutoCancelExpiredOrderTaskTest {

    @Autowired
    private AutoCancelExpiredOrderTask autoCancelExpiredOrderTask;

    @Test
    public void test() throws Exception {
        autoCancelExpiredOrderTask.execute();
    }

}
