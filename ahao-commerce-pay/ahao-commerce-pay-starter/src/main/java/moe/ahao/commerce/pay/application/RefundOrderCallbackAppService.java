package moe.ahao.commerce.pay.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.pay.infrastructure.gateway.impl.AfterSaleRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class RefundOrderCallbackAppService {
    @Autowired
    private AfterSaleRemote afterSaleRemote;

    public Boolean callback(HttpServletRequest request) {
        return true;
    }
    // public Boolean callback(RefundOrderCallbackCommand command) {
    //     //  模拟数据
    //     Boolean success = afterSaleRemote.refundOrderCallback(command);
    //     return success;
    // }
}
