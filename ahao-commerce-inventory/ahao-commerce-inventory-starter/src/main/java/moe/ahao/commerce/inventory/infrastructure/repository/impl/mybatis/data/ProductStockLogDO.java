package moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;

/**
 * 库存扣减日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_stock_log")
public class ProductStockLogDO extends BaseDO {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 商品sku编号
     */
    private String skuCode;
    /**
     * 原始销售库存
     */
    private BigDecimal originSaleStockQuantity;
    /**
     * 原始已销售库存
     */
    private BigDecimal originSaledStockQuantity;
    /**
     * 扣减后的销售库存
     */
    private BigDecimal deductedSaleStockQuantity;
    /**
     * 增加的已销售库存
     */
    private BigDecimal increasedSaledStockQuantity;
    /**
     * 状态：1-已扣减；2-已释放
     */
    private Integer status;
}
