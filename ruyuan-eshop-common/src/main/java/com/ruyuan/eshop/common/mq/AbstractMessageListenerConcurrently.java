package com.ruyuan.eshop.common.mq;

import com.ruyuan.eshop.common.constants.CoreConstant;
import com.ruyuan.eshop.common.utils.MdcUtil;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Map;

/**
 * 抽象的消费者MessageListener组件
 * @author zhonghuashishan
 * @version 1.0
 */
public abstract class AbstractMessageListenerConcurrently implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            if(msgs != null && !msgs.isEmpty()) {
                Map<String, String> map = msgs.get(0).getProperties();
                String traceId = "";
                if(map != null) {
                    traceId = map.get(CoreConstant.TRACE_ID);
                }
                if(traceId != null && !"".equals(traceId)) {
                    MdcUtil.setTraceId(traceId);
                }
            }
            return onMessage(msgs, context);
        } finally {
            MdcUtil.removeTraceId();
        }
    }

    public abstract ConsumeConcurrentlyStatus onMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context);
}