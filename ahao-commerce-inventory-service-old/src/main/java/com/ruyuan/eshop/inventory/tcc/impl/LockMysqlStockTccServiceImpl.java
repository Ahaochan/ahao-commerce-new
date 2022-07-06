package com.ruyuan.eshop.inventory.tcc.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.constants.CoreConstant;
import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.redis.RedisLock;
import com.ruyuan.eshop.common.utils.LoggerFormat;
import com.ruyuan.eshop.common.utils.MdcUtil;
import com.ruyuan.eshop.inventory.dao.ProductStockDAO;
import com.ruyuan.eshop.inventory.dao.ProductStockLogDAO;
import com.ruyuan.eshop.inventory.domain.dto.DeductStockDTO;
import com.ruyuan.eshop.inventory.domain.entity.ProductStockDO;
import com.ruyuan.eshop.inventory.domain.entity.ProductStockLogDO;
import com.ruyuan.eshop.inventory.tcc.LockMysqlStockTccService;
import com.ruyuan.eshop.inventory.tcc.TccResultHolder;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LockMysqlStockTccServiceImpl implements LockMysqlStockTccService {

    @Autowired
    private ProductStockDAO productStockDAO;

    @Autowired
    private ProductStockLogDAO productStockLogDAO;

    @Autowired
    private RedisLock redisLock;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deductStock(BusinessActionContext actionContext, DeductStockDTO deductStock, String traceId) {
        String xid = actionContext.getXid();
        String skuCode = deductStock.getSkuCode();
        Integer saleQuantity = deductStock.getSaleQuantity();

        // 标识try阶段开始执行
        TccResultHolder.tagTryStart(getClass(), skuCode, xid);

        // 悬挂问题：rollback接口比try接口先执行，即rollback接口进行了空回滚，try接口才执行，导致try接口预留的资源无法被取消
        // 解决空悬挂的思路：即当rollback接口出现空回滚时，需要打一个标识（在数据库中查一条记录），在try这里判断一下
        if (isEmptyRollback()) {
            return false;
        }

        log.info(LoggerFormat.build()
                .remark("一阶段方法：扣减mysql销售库存")
                .data("deductStock", deductStock)
                .data("xid", xid)
                .finish());
        // 1、扣减销售库存
        int result = productStockDAO.deductSaleStock(skuCode, saleQuantity);

        if (result > 0) {
            // 2、插入一条扣减日志表
            ProductStockLogDO logDO = buildStockLog(deductStock);
            log.info(LoggerFormat.build()
                    .remark("插入一条扣减日志表")
                    .data("logDO", logDO)
                    .finish());
            productStockLogDAO.save(logDO);

            //标识try阶段执行成功
            TccResultHolder.tagTrySuccess(getClass(), skuCode, xid);
        }

        return result > 0;
    }

    @Override
    public void commit(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        DeductStockDTO deductStock = ((JSONObject) actionContext.getActionContext("deductStock")).toJavaObject(DeductStockDTO.class);
        String traceId = (String) actionContext.getActionContext("traceId");
        MdcUtil.setUserTraceId(traceId);

        String skuCode = deductStock.getSkuCode();
        Integer saleQuantity = deductStock.getSaleQuantity();

        log.info(LoggerFormat.build()
                .remark("二阶段方法：增加mysql已销售库存")
                .data("deductStock", deductStock)
                .data("xid", xid)
                .finish());

        //幂等
        // 当出现网络异常或者TC Server异常时，会出现重复调用commit阶段的情况，所以需要进行幂等操作
        if (!TccResultHolder.isTrySuccess(getClass(), skuCode, xid)) {
            return;
        }
        //增加已销售库存
        productStockDAO.increaseSaledStock(skuCode, saleQuantity);

        //移除标识
        TccResultHolder.removeResult(getClass(), skuCode, xid);
    }

    @Override
    public void rollback(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        DeductStockDTO deductStock = ((JSONObject) actionContext.getActionContext("deductStock")).toJavaObject(DeductStockDTO.class);
        String traceId = (String) actionContext.getActionContext("traceId");
        MdcUtil.setUserTraceId(traceId);

        String skuCode = deductStock.getSkuCode();
        String orderId = deductStock.getOrderId();
        Integer saleQuantity = deductStock.getSaleQuantity();

        log.info(LoggerFormat.build()
                .remark("回滚：增加mysql销售库存")
                .data("deductStock", deductStock)
                .data("xid", xid)
                .finish());

        //空回滚处理
        if (TccResultHolder.isTagNull(getClass(), skuCode, xid)) {
            log.error(LoggerFormat.build()
                    .remark("mysql:出现空回滚")
                    .data("deductStock", deductStock)
                    .data("xid", xid)
                    .finish());
            insertEmptyRollbackTag();
            return;
        }

        //幂等处理
        //try阶段没有完成的情况下，不必执行回滚，因为try阶段有本地事务，事务失败时已经进行了回滚
        //如果try阶段成功，而其他全局事务参与者失败，这里会执行回滚
        if (!TccResultHolder.isTrySuccess(getClass(), skuCode, xid)) {
            log.info(LoggerFormat.build()
                    .remark("mysql:无需回滚")
                    .data("deductStock", deductStock)
                    .data("xid", xid)
                    .finish());
            return;
        }

        //加扣库存的锁
        String lockKey = RedisLockKeyConstants.DEDUCT_PRODUCT_STOCK_KEY + skuCode;
        try {
            redisLock.lock(lockKey);

            //1、还原销售库存
            productStockDAO.restoreSaleStock(skuCode, saleQuantity);
            //2、插入一个反做log
            productStockLogDAO.save(buildReverseStockLog(deductStock));

        }finally {
            redisLock.unlock(lockKey);
        }


        //移除标识
        TccResultHolder.removeResult(getClass(), skuCode, xid);
    }

    /**
     * 判断是否发生的空回滚
     *
     * @return
     */
    private Boolean isEmptyRollback() {
        //需要查询本地数据库，看是否发生了空回滚
        return false;
    }

    /**
     * 插入空回滚标识
     */
    private void insertEmptyRollbackTag() {
        //在数据库插入空回滚的标识
    }

    /**
     * 构建扣减库存日志
     *
     * @param deductStock
     * @return
     */
    private ProductStockLogDO buildStockLog(DeductStockDTO deductStock) {

        ProductStockDO productStockDO = deductStock.getProductStockDO();
        Long saleQuantity = deductStock.getSaleQuantity().longValue();
        Long originSaleStock = productStockDO.getSaleStockQuantity();
        // 通过扣减log获取原始已销售库存
        Long originSaledStock = getOriginSaledStock(productStockDO);

        ProductStockLogDO logDO = new ProductStockLogDO();
        logDO.setOrderId(deductStock.getOrderId());
        logDO.setSkuCode(deductStock.getSkuCode());
        logDO.setOriginSaleStockQuantity(originSaleStock);
        logDO.setOriginSaledStockQuantity(originSaledStock);
        logDO.setDeductedSaleStockQuantity(originSaleStock - saleQuantity);
        logDO.setIncreasedSaledStockQuantity(originSaledStock + saleQuantity);
        return logDO;
    }

    /**
     * 构建逆向扣减库存日志
     *
     * eg:
     *
     * 1、刚开始的时候，假设sku001的销售库存为1000,已销售库存为0，库存表中的记录如下所示
     * ｜sku_code ｜ sale_stock ｜ saled_stock｜
     * ｜ sku001  ｜    1000    ｜    0       ｜
     *
     * 2、有一个用户1对sku001下单，下单数量为10，mysql的try执行完后，mysql库存表和库存日志表的记录如下所示
     * ｜sku_code ｜ sale_stock ｜ saled_stock｜
     * ｜ sku001  ｜    990     ｜    0      ｜
     *
     * ｜sku_code ｜ order_id   ｜ origin_sale_stock ｜ origin_saled_stock｜ deducted_sale_stock ｜ increased_saled_stock ｜
     * ｜ sku001  ｜    o001    ｜      1000         ｜         0         ｜        990          ｜         10            ｜
     *
     * 3、然后在执行redis的try阶段报错，需要回滚，回滚的时候不能简单的将try阶段插入的log给删除，而是要基于最新的log插入一条逆向扣减日志，
     * 因为rollback是seata server异步调用的，在这个过程中，可能已经有另外的用户已经下单了
     *
     * 假设在seata server要对o001订单进行扣库存回滚之前(调用rollback之前)，有一个用户2已经对sku001下单成功了，下单数量为20，此时mysql库存表和库存日志表的记录如下所示
     * ｜sku_code ｜ sale_stock ｜ saled_stock｜
     * ｜ sku001  ｜    970     ｜    20      ｜
     *
     * ｜sku_code ｜ order_id   ｜ origin_sale_stock ｜ origin_saled_stock｜ deducted_sale_stock ｜ increased_saled_stock ｜
     * ｜ sku001  ｜    o001    ｜      1000         ｜         0         ｜        990          ｜         10            ｜
     * ｜ sku001  ｜    o002    ｜      990          ｜         10        ｜        970          ｜         30            ｜
     *
     * 4、于是在对o001进行rollback的时候，需要基于最新的扣减库存log进行反做，插入逆向的log：saled_stock+saleQuantity，saled_stock-saleQuantity
     * rollback执行完毕后，mysql库存表和库存日志表的记录如下所示
     * ｜sku_code ｜ sale_stock ｜ saled_stock｜
     * ｜ sku001  ｜    980     ｜    20      ｜
     *
     * ｜sku_code ｜ order_id   ｜ origin_sale_stock ｜ origin_saled_stock｜ deducted_sale_stock ｜ increased_saled_stock ｜
     * ｜ sku001  ｜    o001    ｜      1000         ｜         0         ｜        990          ｜         10            ｜
     * ｜ sku001  ｜    o002    ｜      990          ｜         10        ｜        970          ｜         30            ｜
     * ｜ sku001  ｜    o001    ｜      1000         ｜         0         ｜        980          ｜         20            ｜<---逆向扣减日志
     *
     * 5、这样，下一笔订单，就可以基于最新的那一条逆向扣减日志去进行扣减扣库存了，
     * 假设接着有一个用户3已经对sku001下单，并成功，下单数量为30，此时mysql库存表和库存日志表的记录如下所示
     * ｜sku_code ｜ sale_stock ｜ saled_stock｜
     * ｜ sku001  ｜    950     ｜    50      ｜
     *
     * ｜sku_code ｜ order_id   ｜ origin_sale_stock ｜ origin_saled_stock｜ deducted_sale_stock ｜ increased_saled_stock ｜
     * ｜ sku001  ｜    o001    ｜      1000         ｜         0         ｜        990          ｜         10            ｜
     * ｜ sku001  ｜    o002    ｜      990          ｜         10        ｜        970          ｜         30            ｜
     * ｜ sku001  ｜    o001    ｜      1000         ｜         0         ｜        980          ｜         20            ｜
     * ｜ sku001  ｜    o003    ｜      980          ｜         20        ｜        950          ｜         50            ｜
     * @param deductStock
     * @return
     */
    private ProductStockLogDO buildReverseStockLog(DeductStockDTO deductStock) {

        ProductStockDO productStockDO = deductStock.getProductStockDO();
        Long saleQuantity = deductStock.getSaleQuantity().longValue();

        //查询sku库存最近一笔扣减日志
        ProductStockLogDO latestLog = productStockLogDAO.getLatestOne(productStockDO.getSkuCode());

        ProductStockLogDO reverseStockLog = new ProductStockLogDO();
        reverseStockLog.setOrderId(deductStock.getOrderId());
        reverseStockLog.setSkuCode(deductStock.getSkuCode());
        reverseStockLog.setOriginSaleStockQuantity(latestLog.getOriginSaleStockQuantity()+saleQuantity);
        reverseStockLog.setOriginSaledStockQuantity(latestLog.getOriginSaledStockQuantity()-saleQuantity);
        reverseStockLog.setDeductedSaleStockQuantity(latestLog.getDeductedSaleStockQuantity()+saleQuantity);
        reverseStockLog.setIncreasedSaledStockQuantity(latestLog.getIncreasedSaledStockQuantity()-saleQuantity);
        return reverseStockLog;
    }

    /**
     * 获取sku的原始已销售库存
     *
     * @param productStockDO
     * @return
     */
    private Long getOriginSaledStock(ProductStockDO productStockDO) {
        //1、查询sku库存最近一笔扣减日志
        ProductStockLogDO latestLog = productStockLogDAO.getLatestOne(productStockDO.getSkuCode());

        //2、获取原始的已销售库存
        Long originSaledStock = null;
        if (null == latestLog) {
            //第一次扣，直接取productStockDO的saledStockQuantity
            originSaledStock = productStockDO.getSaledStockQuantity();
        } else {
            //取最近一笔扣减日志的increasedSaledStockQuantity
            originSaledStock = latestLog.getIncreasedSaledStockQuantity();
        }
        return originSaledStock;
    }

}
