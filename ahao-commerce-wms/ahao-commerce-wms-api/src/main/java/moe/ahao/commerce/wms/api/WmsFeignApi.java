package moe.ahao.commerce.wms.api;

import moe.ahao.commerce.wms.api.command.CancelPickGoodsCommand;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import moe.ahao.commerce.wms.api.dto.PickDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 仓储系统api
 */
public interface WmsFeignApi {
    String PATH = "/api/wms/";

    /**
     * 捡货
     */
    @PostMapping("/pickGoods")
    Result<PickDTO> pickGoods(PickGoodsCommand command);

    /**
     * 取消捡货
     */
    @PostMapping("/cancelPickGoods")
    Result<Boolean> cancelPickGoods(CancelPickGoodsCommand command);
}
