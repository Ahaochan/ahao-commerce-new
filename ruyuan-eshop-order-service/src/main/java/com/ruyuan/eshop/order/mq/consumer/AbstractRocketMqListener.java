package com.ruyuan.eshop.order.mq.consumer;

import com.ruyuan.eshop.common.constants.CoreConstant;
import com.ruyuan.eshop.common.utils.MdcUtil;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;

import java.nio.charset.StandardCharsets;

/**
 * 抽象的消费者MessageListener组件
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public abstract class AbstractRocketMqListener implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt message) {
        try {
            String traceId = message.getProperty(CoreConstant.TRACE_ID);
            if (traceId != null && !"".equals(traceId)) {
                MdcUtil.setTraceId(traceId);
            }
            onMessage(new String(message.getBody(), StandardCharsets.UTF_8));
        } finally {
            MdcUtil.removeTraceId();
        }
    }

    public abstract void onMessage(String message);
}