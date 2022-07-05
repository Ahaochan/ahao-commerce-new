package com.ruyuan.eshop.common.mq;

import com.ruyuan.eshop.common.constants.CoreConstant;
import com.ruyuan.eshop.common.utils.MdcUtil;
import org.apache.rocketmq.common.message.Message;

/**
 * 自定义扩展的mq消息对象
 * @author zhonghuashishan
 * @version 1.0
 */
public class MQMessage extends Message {

    private static final long serialVersionUID = 8994186609218737102L;

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