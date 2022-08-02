package com.ruyuan.eshop.inventory.mq.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.mq.AbstractMessageListenerConcurrently;
import com.ruyuan.eshop.inventory.api.InventoryApi;
import com.ruyuan.eshop.inventory.domain.request.ReleaseProductStockRequest;
import com.ruyuan.eshop.inventory.exception.InventoryBizException;
import com.ruyuan.eshop.inventory.exception.InventoryErrorCodeEnum;
import com.ruyuan.eshop.order.api.AfterSaleQueryApi;
import com.ruyuan.eshop.order.domain.dto.AfterSaleItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 监听释放库存消息
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class AfterSaleReleaseInventoryListener extends AbstractMessageListenerConcurrently {

    @DubboReference(version = "1.0.0")
    private InventoryApi inventoryApi;

    @DubboReference(version = "1.0.0")
    private AfterSaleQueryApi afterSaleQueryApi;

    @Override
    public ConsumeConcurrentlyStatus onMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            for (MessageExt msg : list) {
                String message = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.info("释放库存消息监听器收到message:{}", message);
                //  封装释放库存参数
                ReleaseProductStockRequest releaseProductStockRequest = buildReleaseProductStock(message);
                //  释放库存
                JsonResult<Boolean> jsonResult = inventoryApi.releaseProductStock(releaseProductStockRequest);
                if (!jsonResult.getSuccess()) {
                    throw new InventoryBizException(InventoryErrorCodeEnum.CONSUME_MQ_FAILED);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("consumer error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    /**
     * 组装释放库存数据
     */
    private ReleaseProductStockRequest buildReleaseProductStock(String message) {
        Map paramMap = JSONObject.parseObject(message, Map.class);
        String orderId = String.valueOf(paramMap.get("orderId"));
        String skuCode = String.valueOf(paramMap.get("skuCode"));

        //  查询订单条目
        AfterSaleItemDTO afterSaleItemDTO = afterSaleQueryApi.getOrderItemByOrderIdAndSkuCode(orderId, skuCode);
        List<ReleaseProductStockRequest.OrderItemRequest> orderItemRequestList = new ArrayList<>();

        ReleaseProductStockRequest.OrderItemRequest orderItemRequest = new ReleaseProductStockRequest.OrderItemRequest();
        orderItemRequest.setSkuCode(skuCode);
        orderItemRequest.setSaleQuantity(afterSaleItemDTO.getReturnQuantity());
        orderItemRequestList.add(orderItemRequest);

        ReleaseProductStockRequest releaseProductStockRequest = new ReleaseProductStockRequest();
        releaseProductStockRequest.setOrderId(orderId);
        releaseProductStockRequest.setOrderItemRequestList(orderItemRequestList);

        return releaseProductStockRequest;
    }
}