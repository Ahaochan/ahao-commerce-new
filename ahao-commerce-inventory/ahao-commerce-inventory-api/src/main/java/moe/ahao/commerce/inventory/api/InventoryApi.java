package moe.ahao.commerce.inventory.api;

import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.domain.entity.Result;

public interface InventoryApi {
    /**
     * 扣减商品库存
     */
    Result<Boolean> deductProductStock(DeductProductStockCommand command);
}
