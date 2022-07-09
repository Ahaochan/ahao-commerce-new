package moe.ahao.commerce.inventory.api.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 添加商品库存
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductStockCommand {
    /**
     * 商品sku编号
     */
    private String skuCode;
    /**
     * 销售库存
     */
    private BigDecimal saleStockQuantity;
}
