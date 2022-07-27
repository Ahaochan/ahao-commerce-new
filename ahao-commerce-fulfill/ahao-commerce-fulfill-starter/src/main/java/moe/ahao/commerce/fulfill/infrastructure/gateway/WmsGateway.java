package moe.ahao.commerce.fulfill.infrastructure.gateway;


import moe.ahao.commerce.fulfill.infrastructure.exception.FulfillExceptionEnum;
import moe.ahao.commerce.fulfill.infrastructure.gateway.feign.WmsFeignClient;
import moe.ahao.commerce.wms.api.command.CancelPickGoodsCommand;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import moe.ahao.commerce.wms.api.dto.PickDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * wms服务远程接口
 */
@Component
public class WmsGateway {
    @Autowired
    private WmsFeignClient wmsFeignClient;

    /**
     * 捡货
     */
    public PickDTO pickGoods(PickGoodsCommand request) {
        Result<PickDTO> result = wmsFeignClient.pickGoods(request);
        if (result.getCode() != Result.SUCCESS) {
            throw FulfillExceptionEnum.WMS_IS_ERROR.msg(result.getMsg());
        }
        return result.getObj();
    }

    /**
     * 取消捡货
     */
    public void cancelPickGoods(String orderId) {
        CancelPickGoodsCommand command = new CancelPickGoodsCommand();
        command.setOrderId(orderId);
        Result<Boolean> result = wmsFeignClient.cancelPickGoods(command);
        if (result.getCode() != Result.SUCCESS) {
            throw FulfillExceptionEnum.WMS_IS_ERROR.msg(result.getMsg());
        }
    }
}
