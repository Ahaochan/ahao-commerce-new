package com.ruyuan.eshop.order.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.ruyuan.eshop.common.constants.RocketMqConstant;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import com.ruyuan.eshop.common.mq.AbstractMessageListenerConcurrently;
import com.ruyuan.eshop.order.dao.OrderItemDAO;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.entity.OrderItemDO;
import com.ruyuan.eshop.order.domain.request.CancelOrderAssembleRequest;
import com.ruyuan.eshop.order.mq.producer.DefaultProducer;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.inventory.api.event.ReleaseProductStockEvent;
import moe.ahao.commerce.market.api.command.ReleaseUserCouponCommand;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听 释放资产消息
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class ReleaseAssetsListener extends AbstractMessageListenerConcurrently {

    @Autowired
    private DefaultProducer defaultProducer;

    @Autowired
    private OrderItemDAO orderItemDAO;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt messageExt : list) {
                // 1、消费到释放资产message
                String message = new String(messageExt.getBody());
                log.info("ReleaseAssetsListener message:{}", message);
                CancelOrderAssembleRequest cancelOrderAssembleRequest = JSONObject.parseObject(message, CancelOrderAssembleRequest.class);
                OrderInfoDTO orderInfoDTO = cancelOrderAssembleRequest.getOrderInfoDTO();
                // 2、发送取消订单退款请求MQ
                if (orderInfoDTO.getOrderStatus() > OrderStatusEnum.CREATED.getCode()) {
                    defaultProducer.sendMessage(RocketMqConstant.CANCEL_REFUND_REQUEST_TOPIC,
                            JSONObject.toJSONString(cancelOrderAssembleRequest), "取消订单退款", null, orderInfoDTO.getOrderId());
                }

                // 3、发送释放库存MQ
                ReleaseProductStockEvent releaseProductStockRequest = buildReleaseProductStock(orderInfoDTO, orderItemDAO);
                defaultProducer.sendMessage(RocketMqConstant.CANCEL_RELEASE_INVENTORY_TOPIC,
                        JSONObject.toJSONString(releaseProductStockRequest), "取消订单释放库存", null, orderInfoDTO.getOrderId());

                // 4、发送释放优惠券MQ
                if (!Strings.isNullOrEmpty(orderInfoDTO.getCouponId())) {
                    ReleaseUserCouponCommand releaseUserCouponRequest = buildReleaseUserCoupon(orderInfoDTO);
                    defaultProducer.sendMessage(RocketMqConstant.CANCEL_RELEASE_PROPERTY_TOPIC,
                            JSONObject.toJSONString(releaseUserCouponRequest), "取消订单释放优惠券", null, orderInfoDTO.getOrderId());
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    /**
     * 组装释放优惠券数据
     *
     * @return
     */
    private ReleaseUserCouponCommand buildReleaseUserCoupon(OrderInfoDTO orderInfoDTO) {
        ReleaseUserCouponCommand releaseUserCouponRequest = new ReleaseUserCouponCommand();
        releaseUserCouponRequest.setCouponId(orderInfoDTO.getCouponId());
        releaseUserCouponRequest.setUserId(orderInfoDTO.getUserId());
        releaseUserCouponRequest.setOrderId(orderInfoDTO.getOrderId());
        return releaseUserCouponRequest;
    }

    /**
     * 组装释放库存数据
     */
    private ReleaseProductStockEvent buildReleaseProductStock(OrderInfoDTO orderInfoDTO, OrderItemDAO orderItemDAO) {
        String orderId = orderInfoDTO.getOrderId();
        List<ReleaseProductStockEvent.OrderItem> orderItemRequestList = new ArrayList<>();

        //  查询订单条目
        ReleaseProductStockEvent.OrderItem orderItemRequest;
        List<OrderItemDO> orderItemDOList = orderItemDAO.listByOrderId(orderId);
        for (OrderItemDO orderItemDO : orderItemDOList) {
            orderItemRequest = new ReleaseProductStockEvent.OrderItem();
            orderItemRequest.setSkuCode(orderItemDO.getSkuCode());
            orderItemRequest.setSaleQuantity(orderItemDO.getSaleQuantity());

            orderItemRequestList.add(orderItemRequest);
        }

        ReleaseProductStockEvent releaseProductStockRequest = new ReleaseProductStockEvent();
        releaseProductStockRequest.setOrderId(orderInfoDTO.getOrderId());
        releaseProductStockRequest.setOrderItems(orderItemRequestList);

        return releaseProductStockRequest;
    }

}
