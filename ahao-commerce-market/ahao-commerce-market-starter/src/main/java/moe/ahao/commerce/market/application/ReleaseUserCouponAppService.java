package moe.ahao.commerce.market.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.market.api.command.ReleaseUserCouponEvent;
import moe.ahao.commerce.market.infrastructure.enums.CouponUsedStatusEnum;
import moe.ahao.commerce.market.infrastructure.exception.MarketExceptionEnum;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data.CouponDO;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.mapper.CouponMapper;
import moe.ahao.exception.CommonBizExceptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
public class ReleaseUserCouponAppService {
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private RedissonClient redissonClient;

    public Boolean releaseUserCoupon(ReleaseUserCouponEvent command) {
        log.info("开始执行回滚优惠券, couponId:{}", command.getCouponId());
        String couponId = command.getCouponId();
        String lockKey = RedisLockKeyConstants.RELEASE_COUPON_KEY + couponId;

        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw MarketExceptionEnum.RELEASE_COUPON_FAILED.msg();
        }
        try {
            // 执行释放优惠券
            Boolean result = ((ReleaseUserCouponAppService) AopContext.currentProxy()).releaseUserCouponWithTx(command);
            return result;
        } finally {
            log.info("回滚优惠券成功, couponId:{}", command.getCouponId());
            lock.unlock();
        }
    }
    /**
     * 释放用户优惠券
     * 这里不会有并发问题, 数据库会加上行锁
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean releaseUserCouponWithTx(ReleaseUserCouponEvent command) {
        // 1. 检查入参
        this.check(command);

        // 2. 获取优惠券信息
        String userId = command.getUserId();
        String couponId = command.getCouponId();
        CouponDO couponAchieve = couponMapper.selectOneByUserIdAndCouponId(userId, couponId);
        if (couponAchieve == null) {
            throw MarketExceptionEnum.USER_COUPON_IS_NULL.msg();
        }
        // 3. 判断优惠券是否已经使用了
        if (!Objects.equals(CouponUsedStatusEnum.USED.getCode(), couponAchieve.getUsed())) {
            log.info("当前用户未使用优惠券,不用回退,userId:{},couponId:{}", userId, couponId);
            return true;
        }

        // 4. 释放优惠券
        couponMapper.updateUsedById(CouponUsedStatusEnum.UN_USED.getCode(), null, couponAchieve.getId());
        return true;
    }

    private void check(ReleaseUserCouponEvent command) {
        String userId = command.getUserId();
        String couponId = command.getCouponId();
        if (StringUtils.isAnyEmpty(userId, couponId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
    }
}
