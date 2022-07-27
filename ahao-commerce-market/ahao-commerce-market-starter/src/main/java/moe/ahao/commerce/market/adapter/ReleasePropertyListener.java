package moe.ahao.commerce.market.adapter;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.market.api.command.ReleaseUserCouponEvent;
import moe.ahao.commerce.market.application.ReleaseUserCouponAppService;
import moe.ahao.commerce.market.infrastructure.exception.MarketExceptionEnum;
import moe.ahao.util.commons.io.JSONHelper;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class ReleasePropertyListener implements MessageListenerConcurrently {
    @Autowired
    private ReleaseUserCouponAppService releaseUserCouponService;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            for (MessageExt msg : msgs) {
                String content = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.info("ReleasePropertyConsumer message:{}", content);

                ReleaseUserCouponEvent command = JSONHelper.parse(content, ReleaseUserCouponEvent.class);
                if(command == null) {
                    throw MarketExceptionEnum.CONSUME_MQ_FAILED.msg();
                }
                // 释放优惠券
                Boolean result = releaseUserCouponService.releaseUserCoupon(command);
                if (Boolean.FALSE.equals(result)) {
                    throw MarketExceptionEnum.CONSUME_MQ_FAILED.msg();
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
