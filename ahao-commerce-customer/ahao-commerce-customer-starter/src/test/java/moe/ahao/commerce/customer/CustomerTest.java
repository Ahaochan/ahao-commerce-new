package moe.ahao.commerce.customer;

import com.ruyuan.eshop.common.enums.AfterSaleTypeEnum;
import com.ruyuan.eshop.common.enums.CustomerAuditResult;
import moe.ahao.commerce.customer.api.command.CustomerReceiveAfterSaleCommand;
import moe.ahao.commerce.customer.api.command.CustomerReviewReturnGoodsCommand;
import moe.ahao.commerce.customer.application.CustomerAuditAppService;
import moe.ahao.commerce.customer.application.ReceivableAfterSaleAppService;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = CustomerApplication.class)
@ActiveProfiles("test")

@EnableAutoConfiguration(exclude = RocketMQAutoConfiguration.class)
public class CustomerTest {
    @Autowired
    private CustomerAuditAppService customerAuditAppService;
    @Autowired
    private ReceivableAfterSaleAppService receivableAfterSaleAppService;

    @Test
    public void receive() {
        CustomerReceiveAfterSaleCommand command = new CustomerReceiveAfterSaleCommand();
        command.setUserId("用户id");
        command.setOrderId("订单id");
        command.setAfterSaleId("售后单id");
        command.setAfterSaleType(AfterSaleTypeEnum.RETURN_MONEY.getCode());
        command.setReturnGoodAmount(new BigDecimal("100"));
        command.setApplyRefundAmount(new BigDecimal("100"));

        //  客服接收订单系统的售后申请
        boolean result = receivableAfterSaleAppService.handler(command);
        Assertions.assertTrue(result);
    }

    @Test
    public void audit() {
        CustomerReviewReturnGoodsCommand command = new CustomerReviewReturnGoodsCommand();
        command.setAfterSaleId("售后单id");
        command.setCustomerId("客服id");
        command.setAuditResult(CustomerAuditResult.ACCEPT.getCode());
        command.setAfterSaleRefundId("售后退款单id");
        command.setOrderId("订单id");
        command.setAuditResultDesc("审核结果描述");

        Boolean success = customerAuditAppService.audit(command);
        Assertions.assertTrue(success);
    }
}
