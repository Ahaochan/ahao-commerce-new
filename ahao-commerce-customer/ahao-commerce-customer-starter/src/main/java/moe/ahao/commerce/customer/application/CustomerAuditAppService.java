package moe.ahao.commerce.customer.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.customer.api.command.CustomerReviewReturnGoodsCommand;
import moe.ahao.commerce.customer.infrastructure.exception.CustomerExceptionEnum;
import moe.ahao.commerce.customer.infrastructure.gateway.AfterSaleGateway;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerAuditAppService {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AfterSaleGateway afterSaleGateway;

    public Boolean audit(CustomerReviewReturnGoodsCommand command) {
        String afterSaleId = command.getAfterSaleId();
        //  分布式锁
        String lockKey = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw CustomerExceptionEnum.CUSTOMER_AUDIT_CANNOT_REPEAT.msg();
        }
        try {
            //  客服审核
            Boolean success = afterSaleGateway.receiveCustomerAuditResult(command);
            return success;
        } finally {
            lock.unlock();
        }
    }
}
