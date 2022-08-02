package com.ruyuan.eshop.fulfill.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruyuan.eshop.common.dao.BaseDAO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillStatusEnum;
import com.ruyuan.eshop.fulfill.mapper.OrderFulfillMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 订单履约表 DAO
 * </p>
 *
 * @author zhonghuashishan
 */
@Repository
public class OrderFulfillDAO extends BaseDAO<OrderFulfillMapper, OrderFulfillDO> {

    /**
     * 保存物流单号
     */
    public boolean saveLogisticsCode(String fulfillId, String logisticsCode) {
        LambdaUpdateWrapper<OrderFulfillDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(OrderFulfillDO::getLogisticsCode, logisticsCode)
                .eq(OrderFulfillDO::getFulfillId, fulfillId);
        return update(updateWrapper);
    }

    /**
     * 查询未履约的订单
     */
    public List<OrderFulfillDO> getNotFulfillCandidates(Integer orderFulfillType) {
        LambdaQueryWrapper<OrderFulfillDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OrderFulfillDO::getStatus, OrderFulfillStatusEnum.FULFILL.getCode())
                .eq(OrderFulfillDO::getOrderFulfillType, orderFulfillType);
        return list(queryWrapper);
    }

    /**
     * 查询履约单
     */
    public OrderFulfillDO getOne(String fulfillId) {
        LambdaQueryWrapper<OrderFulfillDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderFulfillDO::getFulfillId, fulfillId);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 查询履约单
     */
    public List<OrderFulfillDO> listByOrderId(String orderId) {
        LambdaQueryWrapper<OrderFulfillDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderFulfillDO::getOrderId, orderId);
        return list(queryWrapper);
    }

    /**
     * 更新配送员信息
     */
    public boolean updateDeliverer(String fulfillId, String delivererNo, String delivererName, String delivererPhone) {
        LambdaUpdateWrapper<OrderFulfillDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(OrderFulfillDO::getDelivererNo, delivererNo)
                .set(OrderFulfillDO::getDelivererName, delivererName)
                .set(OrderFulfillDO::getDelivererPhone, delivererPhone)
                .eq(OrderFulfillDO::getFulfillId, fulfillId);
        return update(updateWrapper);
    }

    /**
     * 更新履约单状态
     */
    public boolean updateStatus(String fulfillId, Integer fromStatus, Integer toStatus) {
        LambdaUpdateWrapper<OrderFulfillDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(OrderFulfillDO::getStatus, toStatus)
                .eq(OrderFulfillDO::getFulfillId, fulfillId)
                .eq(OrderFulfillDO::getStatus, fromStatus);

        return update(updateWrapper);
    }

    /**
     * 批量更新履约单状态
     */
    public boolean batchUpdateStatus(List<String> fulfillIds, Integer fromStatus, Integer toStatus) {
        LambdaUpdateWrapper<OrderFulfillDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(OrderFulfillDO::getStatus, toStatus)
                .in(OrderFulfillDO::getFulfillId, fulfillIds)
                .eq(OrderFulfillDO::getStatus, fromStatus);

        return update(updateWrapper);
    }
}
