package moe.ahao.commerce.wms.adapter;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.wms.api.WmsFeignApi;
import moe.ahao.commerce.wms.api.command.CancelPickGoodsCommand;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import moe.ahao.commerce.wms.api.dto.PickDTO;
import moe.ahao.commerce.wms.application.CancelPickGoodsAppService;
import moe.ahao.commerce.wms.application.PickGoodsAppService;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(WmsFeignApi.PATH)
public class WmsController implements WmsFeignApi {
    @Autowired
    private PickGoodsAppService pickGoodsAppService;
    @Autowired
    private CancelPickGoodsAppService cancelPickGoodsAppService;

    @Override
    public Result<PickDTO> pickGoods(@RequestBody PickGoodsCommand command) {
        PickDTO pickDTO = pickGoodsAppService.pickGoods(command);
        return Result.success(pickDTO);
    }

    @Override
    public Result<Boolean> cancelPickGoods(@RequestBody CancelPickGoodsCommand command) {
        Boolean success = cancelPickGoodsAppService.cancelPickGoods(command);
        return Result.success(success);
    }
}
