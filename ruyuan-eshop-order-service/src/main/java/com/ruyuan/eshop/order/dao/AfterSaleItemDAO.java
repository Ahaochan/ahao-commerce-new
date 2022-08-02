package com.ruyuan.eshop.order.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruyuan.eshop.common.dao.BaseDAO;
import com.ruyuan.eshop.common.enums.AfterSaleItemTypeEnum;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.mapper.AfterSaleItemMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 订单售后条目表 DAO
 * </p>
 *
 * @author zhonghuashishan
 */
@Repository
public class AfterSaleItemDAO extends BaseDAO<AfterSaleItemMapper, AfterSaleItemDO> {

    /**
     * 根据售后单号查询售后单条目记录
     *
     * @param afterSaleId
     * @return
     */
    public List<AfterSaleItemDO> listByAfterSaleId(String afterSaleId) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleItemDO::getAfterSaleId, afterSaleId);
        return list(queryWrapper);
    }

    /**
     * 查询售后单条目
     *
     * @param afterSaleIds
     * @return
     */
    public List<AfterSaleItemDO> listByAfterSaleIds(List<String> afterSaleIds) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AfterSaleItemDO::getAfterSaleId, afterSaleIds);
        return list(queryWrapper);
    }


    /**
     * 根据订单号查询售后单条目
     */
    public List<AfterSaleItemDO> listByOrderId(String orderId) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleItemDO::getOrderId, orderId);
        return list(queryWrapper);
    }

    /**
     * 根据orderId查询指定订单中returnMark标记的售后条目
     */
    public List<AfterSaleItemDO> listReturnCompletionByOrderId(String orderId, Integer returnCompletionMark) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleItemDO::getOrderId, orderId);
        queryWrapper.eq(AfterSaleItemDO::getReturnCompletionMark, returnCompletionMark);
        return list(queryWrapper);
    }


    /**
     * 根据orderId和skuCode查询售后单条目
     */
    public List<AfterSaleItemDO> getOrderIdAndSkuCode(String orderId, String skuCode) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleItemDO::getOrderId, orderId);
        queryWrapper.eq(AfterSaleItemDO::getSkuCode, skuCode);
        return list(queryWrapper);
    }

    /**
     * 根据orderId、afterSaleId、skuCode查询售后订单条目
     */
    public AfterSaleItemDO getAfterSaleOrderItem(String orderId, String afterSaleId, String skuCode, Integer afterSaleItemMark) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleItemDO::getOrderId, orderId);
        queryWrapper.eq(AfterSaleItemDO::getAfterSaleId, afterSaleId);
        queryWrapper.eq(AfterSaleItemDO::getSkuCode, skuCode);
        queryWrapper.eq(AfterSaleItemDO::getAfterSaleItemType, afterSaleItemMark);
        return getOne(queryWrapper);
    }

    /**
     * 查询出不包含当前afterSaleId的售后条目
     */
    public List<AfterSaleItemDO> listNotContainCurrentAfterSaleId(String orderId, String afterSaleId) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleItemDO::getOrderId, orderId);
        queryWrapper.notIn(AfterSaleItemDO::getAfterSaleId, afterSaleId);
        return list(queryWrapper);
    }

    /**
     * 更新条目售后完成标记
     */
    public Boolean updateAfterSaleItemCompletionMark(String orderId, Integer mark) {
        LambdaUpdateWrapper<AfterSaleItemDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AfterSaleItemDO::getOrderId, orderId)
                .set(AfterSaleItemDO::getReturnCompletionMark, mark);
        return update(updateWrapper);
    }

    /**
     * 提供给撤销订单使用的回退条目售后完成标记
     */
    public Boolean rollbackAfterSaleItemCompletionMark(String orderId, Integer mark, String skuCode) {
        LambdaUpdateWrapper<AfterSaleItemDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AfterSaleItemDO::getOrderId, orderId)
                .eq(AfterSaleItemDO::getSkuCode, skuCode)
                .set(AfterSaleItemDO::getReturnCompletionMark, mark);
        return update(updateWrapper);
    }

    /**
     * 删除售后订单条目
     */
    public boolean delete(String afterSaleId) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleItemDO::getAfterSaleId, afterSaleId);
        return remove(queryWrapper);
    }

    /**
     * 查询全部优惠券售后单和运费售后单
     */
    public List<AfterSaleItemDO> listAfterSaleCouponAndFreight(String orderId) {
        LambdaQueryWrapper<AfterSaleItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleItemDO::getOrderId, orderId);
        queryWrapper.eq(AfterSaleItemDO::getAfterSaleItemType, AfterSaleItemTypeEnum.AFTER_SALE_COUPON.getCode());
        queryWrapper.or();
        queryWrapper.eq(AfterSaleItemDO::getAfterSaleItemType, AfterSaleItemTypeEnum.AFTER_SALE_FREIGHT.getCode());
        return list(queryWrapper);
    }
}
