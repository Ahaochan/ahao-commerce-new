package com.ruyuan.eshop.order.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.common.message.RefundMessage;
import com.ruyuan.eshop.inventory.domain.request.ReleaseProductStockRequest;
import com.ruyuan.eshop.order.converter.OrderConverter;
import com.ruyuan.eshop.order.domain.dto.ReleaseProductStockDTO;
import com.ruyuan.eshop.order.domain.request.AuditPassReleaseAssetsRequest;
import com.ruyuan.eshop.order.mq.consumer.AbstractRocketMqListener;
import com.ruyuan.eshop.order.mq.producer.DefaultProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 接收客服审核通过后的 监听 释放资产消息
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMqConstant.CUSTOMER_AUDIT_PASS_RELEASE_ASSETS_TOPIC,
        consumerGroup = RocketMqConstant.CUSTOMER_AUDIT_PASS_RELEASE_ASSETS_CONSUMER_GROUP,
        selectorExpression = "*",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        consumeThreadMax = 1
)
public class AuditPassReleaseAssetsListener extends AbstractRocketMqListener {

    @Autowired
    private DefaultProducer defaultProducer;

    @Autowired
    private OrderConverter orderConverter;

    @Override
    public void onMessage(String message) {
        // 1、消费到释放资产message
        log.info("AuditPassReleaseAssetsListener message:{}", message);
        AuditPassReleaseAssetsRequest auditPassReleaseAssetsRequest = JSONObject.parseObject(message, AuditPassReleaseAssetsRequest.class);
        // 2、发送释放库存MQ
        ReleaseProductStockDTO releaseProductStockDTO = auditPassReleaseAssetsRequest.getReleaseProductStockDTO();
        ReleaseProductStockRequest releaseProductStockRequest = buildReleaseProductStock(releaseProductStockDTO);
        defaultProducer.sendMessage(RocketMqConstant.AFTER_SALE_RELEASE_INVENTORY_TOPIC,
                JSONObject.toJSONString(releaseProductStockRequest), "客服审核通过释放库存", null, null);

        // 3、发送实际退款
        RefundMessage refundMessage = auditPassReleaseAssetsRequest.getRefundMessage();
        defaultProducer.sendMessage(RocketMqConstant.ACTUAL_REFUND_TOPIC,
                JSONObject.toJSONString(refundMessage), "客服审核通过实际退款", null, null);
    }


    /**
     * 组装释放库存数据
     */
    private ReleaseProductStockRequest buildReleaseProductStock(ReleaseProductStockDTO releaseProductStockDTO) {
        List<ReleaseProductStockRequest.OrderItemRequest> orderItemRequestList = new ArrayList<>();

        //  补充订单条目
        for (ReleaseProductStockDTO.OrderItemRequest releaseProductOrderItemRequest : releaseProductStockDTO.getOrderItemRequestList()) {
            orderItemRequestList.add(orderConverter.convertReleaseStockOrderItemRequest(releaseProductOrderItemRequest));
        }

        ReleaseProductStockRequest releaseProductStockRequest = new ReleaseProductStockRequest();
        releaseProductStockRequest.setOrderId(releaseProductStockDTO.getOrderId());
        releaseProductStockRequest.setOrderItemRequestList(orderItemRequestList);
        releaseProductStockRequest.setSkuCode(releaseProductStockDTO.getSkuCode());

        return releaseProductStockRequest;
    }
}