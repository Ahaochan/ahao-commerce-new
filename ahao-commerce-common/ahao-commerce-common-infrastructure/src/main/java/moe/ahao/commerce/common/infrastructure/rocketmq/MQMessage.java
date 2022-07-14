package moe.ahao.commerce.common.infrastructure.rocketmq;

import moe.ahao.commerce.common.infrastructure.utils.CoreConstant;
import moe.ahao.commerce.common.infrastructure.utils.MdcUtil;
import org.apache.rocketmq.common.message.Message;

/**
 * 自定义扩展的mq消息对象
 */
public class MQMessage extends Message {
    public MQMessage() {
        putTraceId();
    }
    public MQMessage(String topic, byte[] body) {
        super(topic, body);
        putTraceId();
    }
    public MQMessage(String topic, String tags, String keys, int flag, byte[] body, boolean waitStoreMsgOK) {
        super(topic, tags, keys, flag, body, waitStoreMsgOK);
        putTraceId();
    }
    public MQMessage(String topic, String tags, byte[] body) {
        super(topic, tags, body);
        putTraceId();
    }

    public MQMessage(String topic, String tags, String keys, byte[] body) {
        super(topic, tags, keys, body);
        putTraceId();
    }

    private void putTraceId() {
        String traceId = MdcUtil.getTraceId();
        if(traceId != null && !"".equals(traceId)) {
            super.putUserProperty(CoreConstant.TRACE_ID, traceId);
        }
    }
}
