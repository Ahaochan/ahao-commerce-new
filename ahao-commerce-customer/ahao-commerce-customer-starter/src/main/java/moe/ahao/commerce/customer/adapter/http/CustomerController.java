package moe.ahao.commerce.customer.adapter.http;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.customer.api.command.CustomerReviewReturnGoodsCommand;
import moe.ahao.commerce.customer.application.CustomerAuditAppService;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单售后流程controller
 */
@Slf4j
@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    @Autowired
    private CustomerAuditAppService customerAuditAppService;

    /**
     * 客服售后审核
     */
    @PostMapping("/audit")
    public Result<Boolean> audit(@RequestBody CustomerReviewReturnGoodsCommand command) {
        Boolean success = customerAuditAppService.audit(command);
        return Result.success(success);
    }
}
