package moe.ahao.commerce.inventory.application;

import com.ruyuan.eshop.common.utils.MdcUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.inventory.infrastructure.exception.InventoryExceptionEnum;
import moe.ahao.commerce.inventory.infrastructure.tcc.DeductStockDTO;
import moe.ahao.commerce.inventory.infrastructure.tcc.LockMysqlStockTccService;
import moe.ahao.commerce.inventory.infrastructure.tcc.LockRedisStockTccService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 扣减商品库存处理器
 */
@Slf4j
@Component
public class DeductProductStockProcessor {
    @Autowired
    private LockMysqlStockTccService lockMysqlStockTccService;
    @Autowired
    private LockRedisStockTccService lockRedisStockTccService;

    @Autowired
    private SyncStockToCacheProcessor syncStockToCacheProcessor;

    /**
     * 执行扣减商品库存逻辑
     *
     * 外部不能有@Transactional的原因分析
     * try阶段因为外部事务，当try阶段结束时，行锁一直不能释放
     * confirm阶段是seata server远程调用的，所以seata内部会自己开一个事务，来竞争行锁
     * 相当于有两个事务在竞争同一个skuCode=1的行锁
     *
     * 等到try阶段的事务超时，try阶段的事务回滚了，然后confirm阶段的事务拿到行锁，提交事务成功
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void doDeductWithTx(DeductStockDTO deductStock) {
        String skuCode = deductStock.getSkuCode();
        String traceId = MdcUtil.getOrInitTraceId();
        // 1. 执行执行mysql库存扣减
        boolean result = lockMysqlStockTccService.deductStock(null, deductStock, traceId);
        if (!result) {
            throw InventoryExceptionEnum.DEDUCT_PRODUCT_SKU_STOCK_ERROR.msg();
        }

        // 2. 执行redis库存扣减
        result = lockRedisStockTccService.deductStock(null, deductStock, traceId);
        if (!result) {
            // 3. 更新失败, 以mysql数据为准
            log.info("执行redis库存扣减失败, deductStock={}", deductStock);
            syncStockToCacheProcessor.syncStock(skuCode);
        }
    }
}
