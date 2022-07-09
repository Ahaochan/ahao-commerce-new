package moe.ahao.commerce.inventory.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.inventory.infrastructure.cache.LuaScript;
import moe.ahao.commerce.inventory.infrastructure.cache.RedisCacheSupport;
import moe.ahao.commerce.inventory.infrastructure.enums.StockLogStatusEnum;
import moe.ahao.commerce.inventory.infrastructure.exception.InventoryExceptionEnum;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockLogMapper;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockMapper;
import moe.ahao.util.spring.redis.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 释放商品库存处理器
 */
@Slf4j
@Component
public class ReleaseProductStockProcessor {
    @Autowired
    private ProductStockMapper productStockMapper;
    @Autowired
    private ProductStockLogMapper productStockLogMapper;

    /**
     * 执行释放商品库存逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void doReleaseWithTx(String orderId, String skuCode, BigDecimal saleQuantity, Long logId) {
        // 1. 执行mysql库存释放
        int nums = productStockMapper.releaseProductStock(skuCode, saleQuantity);
        if (nums <= 0) {
            throw InventoryExceptionEnum.RELEASE_PRODUCT_SKU_STOCK_ERROR.msg();
        }

        //2、更新库存日志的状态为"已释放"
        if (logId != null) {
            productStockLogMapper.updateStatusById(logId, StockLogStatusEnum.RELEASED.getCode());
        }

        // 3. 执行redis库存释放
        String luaScript = LuaScript.RELEASE_PRODUCT_STOCK;
        String saleStockKey = RedisCacheSupport.SALE_STOCK;
        String saledStockKey = RedisCacheSupport.SALED_STOCK;
        String productStockKey = RedisCacheSupport.buildProductStockKey(skuCode);

        Long result = RedisHelper.getRedisTemplate().execute(new DefaultRedisScript<>(luaScript, Long.class),
            Arrays.asList(productStockKey, saleStockKey, saledStockKey), String.valueOf(saleQuantity));
        if (result == null || result < 0) {
            throw InventoryExceptionEnum.INCREASE_PRODUCT_SKU_STOCK_ERROR.msg();
        }
    }
}
