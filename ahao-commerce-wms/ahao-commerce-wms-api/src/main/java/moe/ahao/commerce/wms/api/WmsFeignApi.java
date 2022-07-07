package moe.ahao.commerce.wms.api;

import moe.ahao.commerce.wms.api.command.CancelPickGoodsCommand;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import moe.ahao.commerce.wms.api.dto.PickDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;

public interface WmsFeignApi extends WmsApi{
    String CONTEXT = "/api/wms/";

    @PostMapping("/pickGoods")
    Result<PickDTO> pickGoods(PickGoodsCommand command);
    @PostMapping("/cancelPickGoods")
    Result<Boolean> cancelPickGoods(CancelPickGoodsCommand command);
}
