package moe.ahao.commerce.customer.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.customer.api.event.CustomerReceiveAfterSaleEvent;
import moe.ahao.commerce.customer.infrastructure.exception.CustomerExceptionEnum;
import moe.ahao.commerce.customer.infrastructure.repository.impl.mybatis.data.CustomerReceivesAfterSaleInfoDO;
import moe.ahao.commerce.customer.infrastructure.repository.impl.mybatis.mapper.CustomerReceivesAfterSaleInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReceivableAfterSaleAppService {
    @Autowired
    private CustomerReceivesAfterSaleInfoMapper customerReceivesAfterSaleInfoMapper;

    @Autowired
    private RedissonClient redissonClient;

    public boolean handler(CustomerReceiveAfterSaleEvent event) {
        //  1. 校验入参
        this.check(event);

        //  2. 分布式锁
        String afterSaleId = event.getAfterSaleId();
        String lockKey = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw CustomerExceptionEnum.PROCESS_RECEIVE_AFTER_SALE_REPEAT.msg();
        }

        try {
            // 3. 保存售后申请数据
            CustomerReceivesAfterSaleInfoDO data = new CustomerReceivesAfterSaleInfoDO();
            // data.setId();
            data.setUserId(event.getUserId());
            data.setOrderId(event.getOrderId());
            data.setAfterSaleId(event.getAfterSaleId());
            data.setAfterSaleRefundId(event.getAfterSaleRefundId());
            data.setAfterSaleType(event.getAfterSaleType());
            data.setReturnGoodAmount(event.getReturnGoodAmount());
            data.setApplyRefundAmount(event.getApplyRefundAmount());
            customerReceivesAfterSaleInfoMapper.insert(data);

            log.info("客服保存售后申请信息成功, afterSaleId:{}", event.getAfterSaleId());
            return true;
        } catch (Exception e) {
            log.error("客服保存售后申请信息失败, afterSaleId:{}", event.getAfterSaleId(), e);
            throw CustomerExceptionEnum.SAVE_AFTER_SALE_INFO_FAILED.msg(e.getMessage());
        } finally {
            // 4、放锁
            lock.unlock();
        }
    }

    private void check(CustomerReceiveAfterSaleEvent event) {
        if (StringUtils.isEmpty(event.getUserId())) {
            throw CustomerExceptionEnum.USER_ID_IS_NULL.msg();
        }
        if (StringUtils.isEmpty(event.getOrderId())) {
            throw CustomerExceptionEnum.ORDER_ID_IS_NULL.msg();
        }
        if (StringUtils.isEmpty(event.getAfterSaleId())) {
            throw CustomerExceptionEnum.AFTER_SALE_ID_IS_NULL.msg();
        }
        if (event.getAfterSaleType() == null) {
            throw CustomerExceptionEnum.AFTER_SALE_TYPE_IS_NULL.msg();
        }
        if (event.getReturnGoodAmount() == null) {
            throw CustomerExceptionEnum.RETURN_GOOD_AMOUNT_IS_NULL.msg();
        }
        if (event.getApplyRefundAmount() == null) {
            throw CustomerExceptionEnum.APPLY_REFUND_AMOUNT_IS_NULL.msg();
        }
    }
}
