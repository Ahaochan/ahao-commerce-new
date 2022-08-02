package com.ruyuan.eshop.inventory.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruyuan.eshop.common.dao.BaseDAO;
import com.ruyuan.eshop.inventory.domain.entity.ProductStockLogDO;
import com.ruyuan.eshop.inventory.enums.StockLogStatusEnum;
import com.ruyuan.eshop.inventory.mapper.ProductStockLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 库存扣减日志表 Mapper 接口
 * </p>
 *
 * @author zhonghuashishan
 */
@Repository
public class ProductStockLogDAO extends BaseDAO<ProductStockLogMapper, ProductStockLogDO> {

    @Autowired
    private ProductStockLogMapper productStockLogMapper;

    /**
     * 查询库存扣减日志
     *
     * @param orderId
     * @param skuCode
     * @return
     */
    public ProductStockLogDO getLog(String orderId, String skuCode) {
        LambdaQueryWrapper<ProductStockLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductStockLogDO::getOrderId, orderId)
                .eq(ProductStockLogDO::getSkuCode, skuCode)
        ;
        return baseMapper.selectOne(queryWrapper);
    }

    public boolean removeLogs(String orderId, String skuCode) {
        LambdaQueryWrapper<ProductStockLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductStockLogDO::getOrderId,orderId)
                .eq(ProductStockLogDO::getSkuCode,skuCode);

        List<ProductStockLogDO> logs = list(queryWrapper);
        for(ProductStockLogDO log : logs) {
            baseMapper.deleteById(log.getId());
        }
        return true;
    }

    /**
     * 查询sku库存最近一笔扣减日志
     * @param skuCode
     * @return
     */
    public ProductStockLogDO getLatestOne(String skuCode) {
        LambdaQueryWrapper<ProductStockLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(ProductStockLogDO::getSkuCode,skuCode)
                .orderByDesc(ProductStockLogDO::getId).last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 更新库存日志状态
     *
     * @return
     */
    public Boolean updateStatus(Long id, StockLogStatusEnum status) {
        LambdaUpdateWrapper<ProductStockLogDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductStockLogDO::getId, id)
                .set(ProductStockLogDO::getStatus, status.getCode())
        ;
        return update(updateWrapper);
    }


}
