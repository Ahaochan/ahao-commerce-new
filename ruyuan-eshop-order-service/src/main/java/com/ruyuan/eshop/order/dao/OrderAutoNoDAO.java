package com.ruyuan.eshop.order.dao;

import com.ruyuan.eshop.common.dao.BaseDAO;
import com.ruyuan.eshop.common.exception.BaseBizException;
import com.ruyuan.eshop.order.domain.entity.OrderAutoNoDO;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.mapper.OrderAutoNoMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 订单编号表 DAO
 * </p>
 *
 * @author zhonghuashishan
 */
@Repository
public class OrderAutoNoDAO extends BaseDAO<OrderAutoNoMapper, OrderAutoNoDO> {

    // 对于一个段的申请，必须包含在一个事务里，多个事务是实现隔离的
    // 我的事务里，累加完毕了以后，我能看到的数据，是我的事务的视图里可以看到的，mvcc的概念，mysql里是有一个多版本隔离机制
    //
    @Transactional(rollbackFor = Exception.class)
    public OrderAutoNoDO updateMaxIdAndGet(String bizTag) {
        int ret = baseMapper.updateMaxId(bizTag);
        if (ret != 1) {
            throw new BaseBizException(OrderErrorCodeEnum.ORDER_AUTO_NO_GEN_ERROR);
        }
        return baseMapper.findByBizTag(bizTag);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderAutoNoDO updateMaxIdByDynamicStepAndGet(String bizTag, int nextStep) {
        // maxid=10000,5000，15000,10000~15000,是你的一个新的分段
        int ret = baseMapper.updateMaxIdByDynamicStep(bizTag, nextStep);
        if (ret != 1) {
            throw new BaseBizException(OrderErrorCodeEnum.ORDER_AUTO_NO_GEN_ERROR);
        }
        return baseMapper.findByBizTag(bizTag);
    }
}