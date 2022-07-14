package moe.ahao.commerce.inventory.api;

import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface InventoryFeignApi {
    String PATH = "/api/inventory/";

    /**
     * 扣减商品库存
     */
    @PostMapping("/deductProductStock")
    Result<Boolean> deductProductStock(@RequestBody DeductProductStockCommand command);
}
