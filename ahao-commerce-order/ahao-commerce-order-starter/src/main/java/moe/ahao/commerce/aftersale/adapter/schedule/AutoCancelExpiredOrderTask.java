package moe.ahao.commerce.aftersale.adapter.schedule;


import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.aftersale.api.command.CancelOrderCommand;
import moe.ahao.commerce.aftersale.application.CancelOrderAppService;
import moe.ahao.commerce.aftersale.infrastructure.enums.OrderCancelTypeEnum;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.order.infrastructure.config.OrderProperties;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 自动取消超时订单任务
 */
@Slf4j
@Component
public class AutoCancelExpiredOrderTask {
    @Autowired
    private CancelOrderAppService cancelOrderAppService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderProperties orderProperties;

    /**
     * 执行任务逻辑
     */
    @XxlJob("autoCancelExpiredOrderTask")
    public void execute() throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int totalShardNum = XxlJobHelper.getShardTotal();
        String param = XxlJobHelper.getJobParam();

        // 扫描所有未支付的订单
        List<OrderInfoDO> orderInfoList = orderInfoMapper.selectListByOrderStatus(OrderStatusEnum.unPaidStatus());
        for (OrderInfoDO orderInfo : orderInfoList) {
            if (totalShardNum <= 0) {
                // 不进行分片
                this.doExecute(orderInfo);
            } else {
                // 分片, 是否当前实例要处理的订单数据
                int hash = this.hash(orderInfo.getOrderId()) % totalShardNum;
                if (hash == shardIndex) {
                    this.doExecute(orderInfo);
                }
            }
        }
        XxlJobHelper.handleSuccess();
    }

    private void doExecute(OrderInfoDO order) {
        if (new Date().getTime() - order.getExpireTime().getTime() >= orderProperties.getExpireTime()) {
            // 超过30min未支付
            CancelOrderCommand command = new CancelOrderCommand();
            command.setOrderId(order.getOrderId());
            command.setUserId(order.getUserId());
            command.setBusinessIdentifier(order.getBusinessIdentifier());
            command.setOrderType(order.getOrderType());
            command.setCancelType(OrderCancelTypeEnum.TIMEOUT_CANCELED.getCode());
            command.setOrderStatus(order.getOrderStatus());
            // command.setOldOrderStatus();
            try {
                cancelOrderAppService.cancel(command);
            } catch (Exception e) {
                log.error("AutoCancelExpiredOrderTask execute error:", e);
            }
        }
    }

    /**
     * hash
     */
    private int hash(String orderId) {
        // 解决取模可能为负数的情况
        return orderId.hashCode() & Integer.MAX_VALUE;
    }
}
