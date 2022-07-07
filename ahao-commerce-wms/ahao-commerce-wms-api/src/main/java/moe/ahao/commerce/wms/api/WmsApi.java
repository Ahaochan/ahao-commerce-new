package moe.ahao.commerce.wms.api;

import moe.ahao.commerce.wms.api.command.CancelPickGoodsCommand;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import moe.ahao.commerce.wms.api.dto.PickDTO;
import moe.ahao.domain.entity.Result;

/**
 * 仓储系统api
 */
public interface WmsApi {
    /**
     * 捡货
     */
    Result<PickDTO> pickGoods(PickGoodsCommand command);

    /**
     * 取消捡货
     */
    Result<Boolean> cancelPickGoods(CancelPickGoodsCommand command);

}
