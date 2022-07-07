package com.ruyuan.eshop.fulfill.remote;


import com.ruyuan.eshop.fulfill.exception.FulfillBizException;
import moe.ahao.commerce.wms.api.WmsApi;
import moe.ahao.commerce.wms.api.command.CancelPickGoodsCommand;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import moe.ahao.commerce.wms.api.dto.PickDTO;
import moe.ahao.domain.entity.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * wms服务远程接口
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class WmsRemote {


    /**
     * 库存服务
     */
    @DubboReference(version = "1.0.0", retries = 0)
    private WmsApi wmsApi;


    /**
     * 捡货
     */
    public PickDTO pickGoods(PickGoodsCommand request) {
        Result<PickDTO> result = wmsApi.pickGoods(request);
        if (result.getCode() != Result.SUCCESS) {
            throw new FulfillBizException(String.valueOf(result.getCode()), result.getMsg());
        }
        return result.getObj();
    }

    /**
     * 取消捡货
     */
    public void cancelPickGoods(String orderId) {
        CancelPickGoodsCommand command = new CancelPickGoodsCommand();
        command.setOrderId(orderId);
        Result<Boolean> result = wmsApi.cancelPickGoods(command);
        if (result.getCode() != Result.SUCCESS) {
            throw new FulfillBizException(String.valueOf(result.getCode()), result.getMsg());
        }
    }

}
