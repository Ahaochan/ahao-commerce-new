package moe.ahao.commerce.inventory.api.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 调整商品sku库存请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyProductStockCommand {
    /**
     * 商品sku编号
     */
    private String skuCode;
    /**
     * 销售库存增量（可正，可负）
     */
    private BigDecimal stockIncremental;
}
