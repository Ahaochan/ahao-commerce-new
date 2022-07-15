package moe.ahao.commerce.customer.application;

import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.customer.api.command.CustomerReceiveAfterSaleCommand;
import moe.ahao.commerce.customer.infrastructure.exception.CustomerExceptionEnum;
import moe.ahao.commerce.customer.infrastructure.gateway.AfterSaleGateway;
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
    private AfterSaleGateway afterSaleRemote;

    @Autowired
    private RedissonClient redissonClient;

    public boolean handler(CustomerReceiveAfterSaleCommand command) {
        //  1. 校验入参
        this.check(command);

        //  2. 分布式锁
        String afterSaleId = command.getAfterSaleId();
        String lockKey = RedisLockKeyConstants.REFUND_KEY + afterSaleId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw CustomerExceptionEnum.PROCESS_RECEIVE_AFTER_SALE_REPEAT.msg();
        }

        try {
            // 3. 获取售后退款单id
            String afterSaleRefundId = afterSaleRemote.customerFindAfterSaleRefundInfo(command);
            // 4. 保存售后申请数据

            CustomerReceivesAfterSaleInfoDO data = new CustomerReceivesAfterSaleInfoDO();
            // data.setId();
            data.setUserId(command.getUserId());
            data.setOrderId(command.getOrderId());
            data.setAfterSaleId(command.getAfterSaleId());
            data.setAfterSaleRefundId(afterSaleRefundId);
            data.setAfterSaleType(command.getAfterSaleType());
            data.setReturnGoodAmount(command.getReturnGoodAmount());
            data.setApplyRefundAmount(command.getApplyRefundAmount());
            customerReceivesAfterSaleInfoMapper.insert(data);

            log.info("客服保存售后申请信息成功, afterSaleId:{}", command.getAfterSaleId());
            return true;
        } catch (Exception e) {
            log.error("客服保存售后申请信息失败, afterSaleId:{}", command.getAfterSaleId(), e);
            throw CustomerExceptionEnum.SAVE_AFTER_SALE_INFO_FAILED.msg(e.getMessage());
        } finally {
            // 4、放锁
            lock.unlock();
        }
    }

    private void check(CustomerReceiveAfterSaleCommand command) {
        if (StringUtils.isEmpty(command.getUserId())) {
            throw CustomerExceptionEnum.USER_ID_IS_NULL.msg();
        }
        if (StringUtils.isEmpty(command.getOrderId())) {
            throw CustomerExceptionEnum.ORDER_ID_IS_NULL.msg();
        }
        if (StringUtils.isEmpty(command.getAfterSaleId())) {
            throw CustomerExceptionEnum.AFTER_SALE_ID_IS_NULL.msg();
        }
        if (command.getAfterSaleType() == null) {
            throw CustomerExceptionEnum.AFTER_SALE_TYPE_IS_NULL.msg();
        }
        if (command.getReturnGoodAmount() == null) {
            throw CustomerExceptionEnum.RETURN_GOOD_AMOUNT_IS_NULL.msg();
        }
        if (command.getApplyRefundAmount() == null) {
            throw CustomerExceptionEnum.APPLY_REFUND_AMOUNT_IS_NULL.msg();
        }
    }
}
