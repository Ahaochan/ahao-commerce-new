package moe.ahao.commerce.fulfill.infrastructure.gateway;


import moe.ahao.commerce.fulfill.infrastructure.exception.FulfillExceptionEnum;
import moe.ahao.commerce.fulfill.infrastructure.gateway.feign.TmsFeignClient;
import moe.ahao.commerce.tms.api.command.CancelSendOutCommand;
import moe.ahao.commerce.tms.api.command.SendOutCommand;
import moe.ahao.commerce.tms.api.dto.SendOutDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * tms服务远程接口
 */
@Component
public class TmsGateway {
    /**
     * 库存服务
     */
    @Autowired
    private TmsFeignClient tmsFeignClient;

    /**
     * 发货
     */
    public SendOutDTO sendOut(SendOutCommand request) {
        Result<SendOutDTO> result = tmsFeignClient.sendOut(request);
        if (result.getCode() != Result.SUCCESS) {
            throw FulfillExceptionEnum.TMS_IS_ERROR.msg(result.getMsg());
        }
        return result.getObj();
    }

    /**
     * 取消发货
     */
    public void cancelSendOut(String orderId) {
        CancelSendOutCommand command = new CancelSendOutCommand(orderId);
        Result<Boolean> result = tmsFeignClient.cancelSendOut(command);
        if (result.getCode() != Result.SUCCESS) {
            throw FulfillExceptionEnum.TMS_IS_ERROR.msg(result.getMsg());
        }
    }
}
