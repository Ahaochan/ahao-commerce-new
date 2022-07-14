package moe.ahao.commerce.fulfill.infrastructure.gateway;


import moe.ahao.commerce.fulfill.infrastructure.exception.FulfillExceptionEnum;
import moe.ahao.commerce.wms.api.WmsFeignApi;
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
    private WmsFeignApi wmsFeignApi;

    /**
     * 捡货
     */
    public PickDTO pickGoods(PickGoodsCommand request) {
        Result<PickDTO> result = wmsFeignApi.pickGoods(request);
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
        Result<Boolean> result = wmsFeignApi.cancelPickGoods(command);
        if (result.getCode() != Result.SUCCESS) {
            throw FulfillExceptionEnum.WMS_IS_ERROR.msg(result.getMsg());
        }
    }
}
