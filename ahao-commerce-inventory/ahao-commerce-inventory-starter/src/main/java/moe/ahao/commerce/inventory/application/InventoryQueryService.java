package moe.ahao.commerce.inventory.application;

import moe.ahao.commerce.inventory.infrastructure.cache.RedisCacheSupport;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockDO;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockMapper;
import moe.ahao.util.spring.redis.RedisHelper;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static moe.ahao.commerce.inventory.infrastructure.cache.RedisCacheSupport.SALED_STOCK;
import static moe.ahao.commerce.inventory.infrastructure.cache.RedisCacheSupport.SALE_STOCK;

@Service
public class InventoryQueryService {
    @Autowired
    private ProductStockMapper productStockMapper;

    public Map<String, Map<String, BigDecimal>> query(String skuCode) {
        Map<String, Map<String, BigDecimal>> result = new HashMap<>();
        result.put("mysql", this.getMySQLData(skuCode));
        result.put("redis", this.getRedisData(skuCode));
        return result;
    }

    private Map<String, BigDecimal> getMySQLData(String skuCode) {
        ProductStockDO productStock = productStockMapper.selectOneBySkuCode(skuCode);
        if (productStock == null) {
            return Collections.emptyMap();
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("saleStockQuantity", productStock.getSaleStockQuantity());
        result.put("saledStockQuantity", productStock.getSaledStockQuantity());
        return result;
    }

    private Map<String, BigDecimal> getRedisData(String skuCode) {
        String productStockKey = RedisCacheSupport.buildProductStockKey(skuCode);
        Map<String, String> productStockValue = RedisHelper.hmget(productStockKey);
        if(MapUtils.isEmpty(productStockValue)) {
            return Collections.emptyMap();
        }

        Map<String, BigDecimal> result = new HashMap<>();
        Optional.ofNullable(productStockValue.get(SALE_STOCK)).map(BigDecimal::new).ifPresent(s -> result.put(SALE_STOCK, s));
        Optional.ofNullable(productStockValue.get(SALED_STOCK)).map(BigDecimal::new).ifPresent(s -> result.put(SALED_STOCK, s));
        return result;
    }
}
