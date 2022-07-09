package moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 库存中心的商品库存表 Mapper 接口
 */
@Mapper
public interface ProductStockMapper extends BaseMapper<ProductStockDO> {
    ProductStockDO selectOneBySkuCode(@Param("skuCode") String skuCode);

    /**
     * 扣减商品库存
     */
    int deductProductStock(@Param("skuCode") String skuCode, @Param("saleQuantity") BigDecimal saleQuantity);

    /**
     * 扣减销售库存
     */
    int deductSaleStock(@Param("skuCode") String skuCode, @Param("saleQuantity") BigDecimal saleQuantity);

    /**
     * 增加销售库存
     */
    int increaseSaledStock(@Param("skuCode") String skuCode, @Param("saleQuantity") BigDecimal saleQuantity);

    /**
     * 还原销售库存
     */
    int restoreSaleStock(@Param("skuCode") String skuCode, @Param("saleQuantity") BigDecimal saleQuantity);

    /**
     * 释放商品库存
     */
    int releaseProductStock(@Param("skuCode") String skuCode, @Param("saleQuantity") BigDecimal saleQuantity);

    /**
     * 调整商品库存
     */
    int modifyProductStock(@Param("skuCode") String skuCode, @Param("originSaleQuantity") BigDecimal originSaleQuantity, @Param("saleIncremental") BigDecimal saleIncremental);
}
