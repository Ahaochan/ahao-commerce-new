package moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;

/**
 * 库存中心的商品库存表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory_product_stock")
public class ProductStockDO extends BaseDO {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 商品sku编号
     */
    private String skuCode;
    /**
     * 销售库存
     */
    private BigDecimal saleStockQuantity;
    /**
     * 已销售库存
     */
    private BigDecimal saledStockQuantity;
}
