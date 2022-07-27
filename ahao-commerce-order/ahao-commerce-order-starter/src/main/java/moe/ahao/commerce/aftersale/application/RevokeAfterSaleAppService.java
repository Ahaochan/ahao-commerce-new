package moe.ahao.commerce.aftersale.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.RevokeAfterSaleCommand;
import moe.ahao.commerce.aftersale.infrastructure.component.AfterSaleOperateLogFactory;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusChangeEnum;
import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusEnum;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleLogDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleInfoMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper.AfterSaleLogMapper;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
public class RevokeAfterSaleAppService {
    @Autowired
    private RevokeAfterSaleAppService _this;

    @Autowired
    private AfterSaleInfoMapper afterSaleInfoMapper;
    @Autowired
    private AfterSaleLogMapper afterSaleLogMapper;
    @Autowired
    private AfterSaleOperateLogFactory afterSaleOperateLogFactory;

    @Autowired
    private RedissonClient redissonClient;

    public void revoke(RevokeAfterSaleCommand command) {
        // 1. 参数校验
        String afterSaleId = command.getAfterSaleId();
        if (StringUtils.isEmpty(afterSaleId)) {
            throw OrderExceptionEnum.AFTER_SALE_ID_IS_NULL.msg();
        }

        // 2. 加分布式锁, 锁整个售后单
        // 2.1. 防并发
        // 2.2. 业务上的考虑, 只要涉及售后表的更新, 就需要加锁, 锁整个售后表, 否则算钱的时候, 就会由于突然撤销, 导致钱多算了
        String lockKey = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw OrderExceptionEnum.AFTER_SALE_CANNOT_REVOKE.msg();
        }

        try {
            // 3. 撤销售后单
            _this.doRevoke(command);
        } finally {
            // 4. 释放分布式锁
            lock.unlock();
        }
    }

    /**
     * 撤销售后申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void doRevoke(RevokeAfterSaleCommand command) {
        String afterSaleId = command.getAfterSaleId();
        // 1. 幂等性校验
        AfterSaleInfoDO afterSaleInfo = afterSaleInfoMapper.selectOneByAfterSaleId(afterSaleId);
        if (afterSaleInfo == null) {
            throw OrderExceptionEnum.AFTER_SALE_ID_IS_NULL.msg();
        }
        // 2. 校验售后单是否可以撤销, 只有提交申请状态才可以撤销
        if (!Objects.equals(AfterSaleStatusEnum.COMMITED.getCode(), afterSaleInfo.getAfterSaleStatus())) {
            throw OrderExceptionEnum.AFTER_SALE_CANNOT_REVOKE.msg();
        }
        // 3. 更新售后单状态为 已撤销
        afterSaleInfoMapper.updateAfterSaleStatusByAfterSaleId(afterSaleId, AfterSaleStatusEnum.COMMITED.getCode(), AfterSaleStatusEnum.REVOKE.getCode());
        // 4. 增加一条售后单操作日志
        AfterSaleLogDO afterSaleLog = afterSaleOperateLogFactory.get(afterSaleInfo, AfterSaleStatusChangeEnum.AFTER_SALE_REVOKE);
        afterSaleLogMapper.insert(afterSaleLog);
    }
}
