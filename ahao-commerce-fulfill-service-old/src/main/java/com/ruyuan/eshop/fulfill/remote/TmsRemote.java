package com.ruyuan.eshop.fulfill.remote;


import com.ruyuan.eshop.fulfill.exception.FulfillBizException;
import moe.ahao.commerce.tms.api.TmsApi;
import moe.ahao.commerce.tms.api.command.CancelSendOutCommand;
import moe.ahao.commerce.tms.api.command.SendOutCommand;
import moe.ahao.commerce.tms.api.dto.SendOutDTO;
import moe.ahao.domain.entity.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * tms服务远程接口
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class TmsRemote {


    /**
     * 库存服务
     */
    @DubboReference(version = "1.0.0", retries = 0)
    private TmsApi tmsApi;


    /**
     * 发货
     */
    public SendOutDTO sendOut(SendOutCommand request) {
        Result<SendOutDTO> result = tmsApi.sendOut(request);
        if (result.getCode() != Result.SUCCESS) {
            throw new FulfillBizException(String.valueOf(result.getCode()), result.getMsg());
        }
        return result.getObj();
    }

    /**
     * 取消发货
     */
    public void cancelSendOut(String orderId) {
        CancelSendOutCommand command = new CancelSendOutCommand();
        command.setOrderId(orderId);
        Result<Boolean> result = tmsApi.cancelSendOut(command);
        if (result.getCode() != Result.SUCCESS) {
            throw new FulfillBizException(String.valueOf(result.getCode()), result.getMsg());
        }
    }

}
