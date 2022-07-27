package moe.ahao.commerce.inventory.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.inventory.api.command.ReleaseProductStockCommand;
import moe.ahao.commerce.inventory.infrastructure.cache.RedisCacheSupport;
import moe.ahao.commerce.inventory.infrastructure.enums.StockLogStatusEnum;
import moe.ahao.commerce.inventory.infrastructure.exception.InventoryExceptionEnum;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockDO;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockLogDO;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockLogMapper;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockMapper;
import moe.ahao.exception.CommonBizExceptionEnum;
import moe.ahao.util.spring.redis.RedisHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 释放商品库存处理器
 */
@Slf4j
@Service
public class ReleaseProductStockAppService {
    @Autowired
    private ReleaseProductStockProcessor releaseProductStockProcessor;
    @Autowired
    private AddProductStockProcessor addProductStockProcessor;

    @Autowired
    private ProductStockMapper productStockMapper;
    @Autowired
    private ProductStockLogMapper productStockLogMapper;

    @Autowired
    private RedissonClient redissonClient;


    public boolean releaseProductStock(ReleaseProductStockCommand command) {
        // 1. 检查入参
        this.check(command);

        String orderId = command.getOrderId();
        List<ReleaseProductStockCommand.OrderItem> orderItems = command.getOrderItems();
        for (ReleaseProductStockCommand.OrderItem orderItem : orderItems) {
            String skuCode = orderItem.getSkuCode();
            String modifyLockKey = RedisLockKeyConstants.MODIFY_PRODUCT_STOCK_KEY + skuCode;
            String releaseLockKey = RedisLockKeyConstants.RELEASE_PRODUCT_STOCK_KEY + skuCode;

            // 1. 添加Redis释放库存锁
            // 1.1. 防同一笔订单重复释放
            // 1.2. 重量级锁，保证Mysql+Redis释放库存的原子性，同一时间只能有一个订单来释放，
            //      需要锁查询+扣库存, 获取不到锁, 阻塞等待3秒
            RLock lock = redissonClient.getMultiLock(redissonClient.getLock(modifyLockKey), redissonClient.getLock(releaseLockKey));
            boolean locked = this.tryLock(lock, 5, TimeUnit.SECONDS);
            if (!locked) {
                log.error("无法获取释放库存锁, orderId:{}, skuCode:{}", orderId, skuCode);
                throw InventoryExceptionEnum.RELEASE_PRODUCT_SKU_STOCK_LOCK_CANNOT_ACQUIRE.msg();
            }

            try {
                // 2. 查询Mysql库存数据
                ProductStockDO productStockDO = productStockMapper.selectOneBySkuCode(skuCode);
                log.info("查询mysql库存数据, orderId:{}, productStockDO:{}", orderId, productStockDO);
                if (productStockDO == null) {
                    log.error("商品库存记录不存在, orderId:{}, skuCode:{}", orderId, skuCode);
                    throw InventoryExceptionEnum.PRODUCT_SKU_STOCK_NOT_FOUND_ERROR.msg();
                }

                // 3. 查询Redis库存数据, 如果不存在就初始化Redis缓存
                String productStockKey = RedisCacheSupport.buildProductStockKey(skuCode);
                Map<String, String> productStockValue = RedisHelper.hmget(productStockKey);
                if (productStockValue.isEmpty()) {
                    addProductStockProcessor.initRedis(productStockDO);
                }

                // 4. 查询库存扣减日志, 做幂等性校验
                ProductStockLogDO productStockLog = productStockLogMapper.selectOneByOrderIdAndSkuCode(orderId, skuCode);
                if (productStockLog != null && Objects.equals(StockLogStatusEnum.RELEASED.getCode(), productStockLog.getStatus())) {
                    log.info("已释放过库存, orderId={}, skuCode={}", orderId, skuCode);
                    return true;
                }

                // 5. 释放库存
                Long logId = productStockLog.getId();
                BigDecimal saleQuantity = orderItem.getSaleQuantity();
                releaseProductStockProcessor.doReleaseWithTx(orderId, skuCode, saleQuantity, logId);
            } finally {
                lock.unlock();
            }
        }
        return true;
    }

    /**
     * 检查释放商品库存入参
     */
    private void check(ReleaseProductStockCommand command) {
        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
        List<ReleaseProductStockCommand.OrderItem> orderItems = command.getOrderItems();
        if (CollectionUtils.isEmpty(orderItems)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
    }

    private boolean tryLock(RLock lock, long waitTime, TimeUnit timeUnit) {
        try {
            return lock.tryLock(waitTime, timeUnit);
        } catch (InterruptedException e) {
            log.error("无法获取释放库存锁{}", lock.getName(), e);
            return false;
        }
    }
}
