package moe.ahao.commerce.fulfill.application;

import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.statelang.domain.ExecutionStatus;
import io.seata.saga.statelang.domain.StateMachineInstance;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.constants.RedisLockKeyConstants;
import moe.ahao.commerce.fulfill.api.command.ReceiveFulfillCommand;
import moe.ahao.commerce.fulfill.infrastructure.exception.FulfillExceptionEnum;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.data.OrderFulfillDO;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.mapper.OrderFulfillItemMapper;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.mapper.OrderFulfillMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ReceiveFulfillAppService implements ApplicationContextAware {
    @Setter
    private ApplicationContext applicationContext;

    @Autowired
    private OrderFulfillMapper orderFulfillMapper;
    @Autowired
    private OrderFulfillItemMapper orderFulfillItemMapper;

    @Autowired
    private RedissonClient redissonClient;

    public Boolean fulfill(ReceiveFulfillCommand command) {
        String orderId = command.getOrderId();
        // 1. 加分布式锁（防止重复触发履约）
        String lockKey = RedisLockKeyConstants.ORDER_FULFILL_KEY + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw FulfillExceptionEnum.ORDER_FULFILL_ERROR.msg();
        }

        try {
            // 2. 幂等校验, 校验orderId是否已经履约过
            if (this.orderFulfilled(orderId)) {
                log.info("该订单已履约, orderId={}", orderId);
                return true;
            }

            // 3. saga状态机，触发wms捡货和tms发货
            StateMachineEngine stateMachineEngine = (StateMachineEngine) applicationContext
                .getBean("stateMachineEngine");
            Map<String, Object> startParams = new HashMap<>(3);
            startParams.put("receiveFulfillRequest", command);

            // 配置的saga状态机 json的name
            // 位于/resources/statelang/order_fulfull.json
            String stateMachineName = "order_fulfill";
            log.info("开始触发saga流程，stateMachineName={}", stateMachineName);
            StateMachineInstance inst = stateMachineEngine.startWithBusinessKey(stateMachineName, null, null, startParams);
            if (ExecutionStatus.SU.equals(inst.getStatus())) {
                log.info("订单履约流程执行完毕. xid={}", inst.getId());
            } else {
                log.error("订单履约流程执行异常. xid={}", inst.getId());
                throw FulfillExceptionEnum.ORDER_FULFILL_IS_ERROR.msg();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 校验订单是否履约过
     *
     * @param orderId 订单id
     * @return true为是, false为否
     */
    private boolean orderFulfilled(String orderId) {
        OrderFulfillDO orderFulfill = orderFulfillMapper.selectOneByOrderId(orderId);
        return orderFulfill != null;
    }
}
