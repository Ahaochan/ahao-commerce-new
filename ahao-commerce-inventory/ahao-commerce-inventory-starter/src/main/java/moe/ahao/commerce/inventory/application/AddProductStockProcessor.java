package moe.ahao.commerce.inventory.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.inventory.api.command.AddProductStockCommand;
import moe.ahao.commerce.inventory.infrastructure.cache.RedisCacheSupport;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockDO;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockMapper;
import moe.ahao.util.spring.redis.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 添加商品库存处理器
 */
@Slf4j
@Component
public class AddProductStockProcessor {
    @Autowired
    private ProductStockMapper productStockMapper;

    /**
     * 保存商品库存到redis
     */
    public void initRedis(ProductStockDO productStock) {
        String productStockKey = RedisCacheSupport.buildProductStockKey(productStock.getSkuCode());
        Map<String, String> productStockValue = RedisCacheSupport.buildProductStockValue(productStock.getSaleStockQuantity(), productStock.getSaledStockQuantity());

        RedisHelper.hmset1(productStockKey, productStockValue);
    }

    /**
     * 执行添加商品库存逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void doAddStockWithTx(AddProductStockCommand command) {
        // 1. 构造商品库存DO
        ProductStockDO productStock = new ProductStockDO();
        productStock.setSkuCode(command.getSkuCode());
        productStock.setSaleStockQuantity(command.getSaleStockQuantity());
        productStock.setSaledStockQuantity(BigDecimal.ZERO);

        // 2. 保存商品库存到mysql
        productStockMapper.insert(productStock);

        // 3. 保存商品库存到redis
        this.initRedis(productStock);
    }
}
