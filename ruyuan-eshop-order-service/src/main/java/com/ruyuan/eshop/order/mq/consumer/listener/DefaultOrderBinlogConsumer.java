package com.ruyuan.eshop.order.mq.consumer.listener;

import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.order.mq.consumer.AbstractRocketMqListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于接收canal监听到的订单表日志变化
 * <p>
 * canal兜底任务
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMqConstant.DEFAULT_ORDER_BINLOG,
        consumerGroup = RocketMqConstant.DEFAULT_ORDER_BINLOG_GROUP,
        selectorExpression = "*",
        consumeMode = ConsumeMode.ORDERLY,
        messageModel = MessageModel.CLUSTERING,
        consumeThreadMax = 1
)
public class DefaultOrderBinlogConsumer extends AbstractRocketMqListener {

    @Autowired
    private OrderInfoBinlogConsumer orderInfoBinlogConsumer;

    @Autowired
    private ReverseOrderInfoBinlogConsumer reverseOrderInfoBinlogConsumer;
    /**
     * 已测试
     * DefaultRocketMQListenerContainer#DefaultMessageListenerOrderly
     * 在发生错误时候 会返回ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT
     *
     * @param msg canal监听到的binlog日志
     */
    @SneakyThrows
    @Override
    public void onMessage(String msg) {
        log.info("canal order default 接收到消息 -> {}", msg);

        orderInfoBinlogConsumer.onMessage(msg);
        reverseOrderInfoBinlogConsumer.onMessage(msg);

    }
}