package com.ruyuan.eshop.order.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ruyuan.eshop.common.dao.BaseDAO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleRefundDO;
import com.ruyuan.eshop.order.mapper.AfterSaleRefundMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 售后支付表 DAO
 * </p>
 *
 * @author zhonghuashishan
 */
@Repository
public class AfterSaleRefundDAO extends BaseDAO<AfterSaleRefundMapper, AfterSaleRefundDO> {

    /**
     * 根据售后单号查询售后单支付记录
     *
     * @param afterSaleId
     * @return
     */
    public List<AfterSaleRefundDO> listByAfterSaleId(String afterSaleId) {
        LambdaQueryWrapper<AfterSaleRefundDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleRefundDO::getAfterSaleId, afterSaleId);
        return list(queryWrapper);
    }

    /**
     * 查询售后单退款记录
     *
     * @param afterSaleIds
     * @return
     */
    public List<AfterSaleRefundDO> listByAfterSaleIds(List<String> afterSaleIds) {
        LambdaQueryWrapper<AfterSaleRefundDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AfterSaleRefundDO::getAfterSaleId, afterSaleIds);
        return list(queryWrapper);
    }

    /**
     * 更新售后退款状态
     */
    public boolean updateAfterSaleRefundStatus(AfterSaleRefundDO afterSaleRefundDO) {

        UpdateWrapper<AfterSaleRefundDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("after_sale_id", afterSaleRefundDO.getAfterSaleId());

        return update(afterSaleRefundDO, updateWrapper);
    }

    public AfterSaleRefundDO findAfterSaleRefundByfterSaleId(String afterSaleId) {
        LambdaQueryWrapper<AfterSaleRefundDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleRefundDO::getAfterSaleId, afterSaleId);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 删除售后支付记录
     */
    public boolean delete(String afterSaleId) {
        LambdaQueryWrapper<AfterSaleRefundDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterSaleRefundDO::getAfterSaleId, afterSaleId);
        return remove(queryWrapper);
    }

}
