package com.ruyuan.eshop.order.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruyuan.eshop.common.dao.BaseDAO;
import com.ruyuan.eshop.order.domain.entity.OrderCancelScheduledTaskDO;
import com.ruyuan.eshop.order.mapper.OrderCancelScheduledTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单执行定时取消兜底任务DAO
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Repository
@Slf4j
public class OrderCancelScheduledTaskDAO extends BaseDAO<OrderCancelScheduledTaskMapper, OrderCancelScheduledTaskDO> {

    /**
     * 根据订单号查询订单任务记录
     */
    public OrderCancelScheduledTaskDO getByOrderId(String orderId) {
        LambdaQueryWrapper<OrderCancelScheduledTaskDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderCancelScheduledTaskDO::getOrderId, orderId);
        return getOne(queryWrapper);
    }

    /**
     * 根据订单号删除订单任务记录
     */
    public boolean remove(String orderId) {
        LambdaQueryWrapper<OrderCancelScheduledTaskDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderCancelScheduledTaskDO::getOrderId, orderId);
        return remove(queryWrapper);
    }

    /**
     * 保存订单任务记录
     */
    public boolean saveOne(String orderId, Date expireTime) {
        OrderCancelScheduledTaskDO orderCancelScheduledTaskDO = new OrderCancelScheduledTaskDO();
        orderCancelScheduledTaskDO.setOrderId(orderId);
        orderCancelScheduledTaskDO.setExpireTime(expireTime);
        return saveOrUpdate(orderCancelScheduledTaskDO);
    }

    /**
     * 查询所有支付截止时间 <= 当前时间 的未支付订单记录
     */
    public List<OrderCancelScheduledTaskDO> getUnPaidOrderRecord() {
        QueryWrapper<OrderCancelScheduledTaskDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("expire_time", new Date());
        return list(queryWrapper);
    }
}
