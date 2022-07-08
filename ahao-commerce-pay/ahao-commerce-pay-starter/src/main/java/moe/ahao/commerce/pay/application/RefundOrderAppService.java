package moe.ahao.commerce.pay.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.pay.api.command.RefundOrderCommand;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefundOrderAppService {

    public boolean refund(RefundOrderCommand command) {
        log.info("调用支付接口执行退款,订单号:{},售后单号:{}", command.getOrderId(), command.getAfterSaleId());
        return true;
    }
}
