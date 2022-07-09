package moe.ahao.commerce.inventory.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.commerce.inventory.infrastructure.cache.RedisCacheSupport;
import moe.ahao.commerce.inventory.infrastructure.exception.InventoryExceptionEnum;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockDO;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockLogDO;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockLogMapper;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper.ProductStockMapper;
import moe.ahao.commerce.inventory.infrastructure.tcc.DeductStockDTO;
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
import java.util.concurrent.TimeUnit;

/**
 * 扣减商品库存处理器
 */
@Slf4j
@Service
public class DeductProductStockAppService {
    @Autowired
    private DeductProductStockProcessor deductProductStockProcessor;
    @Autowired
    private AddProductStockProcessor addProductStockProcessor;

    @Autowired
    private ProductStockMapper productStockMapper;
    @Autowired
    private ProductStockLogMapper productStockLogMapper;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 扣减商品库存
     */
    public Boolean deduct(DeductProductStockCommand command) {
        // 1. 检查入参
        this.check(command);

        String orderId = command.getOrderId();
        List<DeductProductStockCommand.OrderItem> orderItems = command.getOrderItems();
        for (DeductProductStockCommand.OrderItem orderItem : orderItems) {
            String skuCode = orderItem.getSkuCode();
            String lockKey = RedisLockKeyConstants.DEDUCT_PRODUCT_STOCK_KEY + skuCode;

            // 1. 添加Redis锁扣库存锁
            // 1.1. 防同一笔订单重复扣减
            // 1.2. 重量级锁，保证Mysql+Redis扣库存的原子性，同一时间只能有一个订单来扣，
            //      需要锁查询+扣库存, 获取不到锁, 阻塞等待3秒
            RLock lock = redissonClient.getLock(lockKey);
            boolean locked = this.tryLock(lock, 5, TimeUnit.SECONDS);
            if (!locked) {
                log.error("无法获取扣减库存锁, orderId:{}, skuCode:{}", orderId, skuCode);
                throw InventoryExceptionEnum.DEDUCT_PRODUCT_SKU_STOCK_CANNOT_ACQUIRE.msg();
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
                    // 如果查询不到redis库存数据，将mysql库存数据放入redis，以mysql的数据为准
                    addProductStockProcessor.initRedis(productStockDO);
                }

                // 4. 查询库存扣减日志, 做幂等性校验
                ProductStockLogDO productStockLog = productStockLogMapper.selectOneByOrderIdAndSkuCode(orderId, skuCode);
                if (productStockLog != null) {
                    log.info("已扣减过，扣减库存日志已存在, orderId={}, skuCode={}", orderId, skuCode);
                    return true;
                }

                // 5. 执行执库存扣减
                BigDecimal saleQuantity = orderItem.getSaleQuantity();
                DeductStockDTO deductStock = new DeductStockDTO(orderId, skuCode, saleQuantity, productStockDO);
                deductProductStockProcessor.doDeductWithTx(deductStock);
            } finally {
                lock.unlock();
            }
        }
        return true;
    }


    /**
     * 检查锁定商品库存入参
     */
    private void check(DeductProductStockCommand deductProductStockRequest) {
        String orderId = deductProductStockRequest.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
        List<DeductProductStockCommand.OrderItem> orderItems = deductProductStockRequest.getOrderItems();
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
