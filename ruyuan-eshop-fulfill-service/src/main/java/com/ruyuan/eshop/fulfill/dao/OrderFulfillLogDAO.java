package com.ruyuan.eshop.fulfill.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruyuan.eshop.common.dao.BaseDAO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillLogDO;
import com.ruyuan.eshop.fulfill.mapper.OrderFulfillLogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 履约单操作日志表 DAO
 * </p>
 *
 * @author zhonghuashishan
 */
@Repository
public class OrderFulfillLogDAO extends BaseDAO<OrderFulfillLogMapper, OrderFulfillLogDO> {

    /**
     * 根据orderId和status查询log
     *
     * @param orderId
     * @param status
     * @return
     */
    public List<OrderFulfillLogDO> listBy(String orderId, Integer status) {
        LambdaQueryWrapper<OrderFulfillLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderFulfillLogDO::getOrderId, orderId)
                .eq(OrderFulfillLogDO::getCurrentStatus, status);
        return list(queryWrapper);
    }

}
