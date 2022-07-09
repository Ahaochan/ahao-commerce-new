package moe.ahao.commerce.inventory.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.inventory.infrastructure.cache.LuaScript;
import moe.ahao.commerce.inventory.infrastructure.cache.RedisCacheSupport;
import moe.ahao.commerce.inventory.infrastructure.exception.InventoryExceptionEnum;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockDO;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockMapper;
import moe.ahao.util.spring.redis.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 调整商品库存处理器
 */
@Slf4j
@Component
public class ModifyProductStockProcessor {
    @Autowired
    private ProductStockMapper productStockMapper;
    @Autowired
    private AddProductStockProcessor addProductStockProcessor;

    /**
     * 调整商品库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void doModifyWithTx(ProductStockDO productStock, BigDecimal stockIncremental) {
        // 1. 更新mysql商品可销售库存数量
        String skuCode = productStock.getSkuCode();
        BigDecimal originSaleStockQuantity = productStock.getSaleStockQuantity();
        int num = productStockMapper.modifyProductStock(skuCode, originSaleStockQuantity, stockIncremental);
        if (num <= 0) {
            throw InventoryExceptionEnum.INCREASE_PRODUCT_SKU_STOCK_ERROR.msg();
        }

        // 2. lua脚本更新redis商品可销售库存数量
        String luaScript = LuaScript.MODIFY_PRODUCT_STOCK;
        String productStockKey = RedisCacheSupport.buildProductStockKey(skuCode);
        String saleStockKey = RedisCacheSupport.SALE_STOCK;
        Long result = RedisHelper.getStringRedisTemplate().execute(new DefaultRedisScript<>(luaScript, Long.class),
            Arrays.asList(productStockKey, saleStockKey), String.valueOf(originSaleStockQuantity), String.valueOf(stockIncremental));

        // 3. redis更新异常, 以mysql的数据为准
        if (result == null || result < 0) {
            RedisHelper.del(saleStockKey);

            ProductStockDO productStockInDB = productStockMapper.selectOneBySkuCode(skuCode);
            addProductStockProcessor.initRedis(productStockInDB);
        }
    }
}
