package com.ruyuan.eshop.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.order.constants.OrderQueryFiledNameConstant;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.dto.EsResponseDTO;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单ES service
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
@Slf4j
public class EsOrderService {

    @Autowired
    private EsClientService esClientService;

    /**
     * 查询订单
     *
     * @param orderId
     * @return
     */
    public OrderInfoDO getOrderInfo(String orderId) {
        try {
            String result = esClientService.queryById(EsIndexNameEnum.ORDER_INFO, orderId);
            return JSONObject.parseObject(result, OrderInfoDO.class);
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 查询订单条目
     *
     * @param orderId
     * @return
     */
    public List<OrderItemDO> listOrderItems(String orderId) {
        try {
            EsResponseDTO esResponseDTO = esClientService.search(EsIndexNameEnum.ORDER_ITEM, buildSearchSourceBuilder(orderId));
            if (esResponseDTO.getTotal() > 0) {
                return esResponseDTO.getData().toJavaList(OrderItemDO.class);
            }
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 查询订单费用明细
     *
     * @param orderId
     * @return
     */
    public List<OrderAmountDetailDO> listOrderAmountDetails(String orderId) {
        try {
            EsResponseDTO esResponseDTO = esClientService.search(EsIndexNameEnum.ORDER_AMOUNT_DETAIL, buildSearchSourceBuilder(orderId));
            if (esResponseDTO.getTotal() > 0) {
                return esResponseDTO.getData().toJavaList(OrderAmountDetailDO.class);
            }
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 查询订单配送信息
     *
     * @param orderId
     * @return
     */
    public OrderDeliveryDetailDO getOrderDeliveryDetail(String orderId) {
        try {
            String result = esClientService.queryById(EsIndexNameEnum.ORDER_DELIVERY_DETAIL, orderId);
            return JSONObject.parseObject(result, OrderDeliveryDetailDO.class);
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 查询订单支付
     *
     * @param orderId
     * @return
     */
    public List<OrderPaymentDetailDO> listOrderPaymentDetails(String orderId) {
        try {
            EsResponseDTO esResponseDTO = esClientService.search(EsIndexNameEnum.ORDER_PAYMENT_DETAIL, buildSearchSourceBuilder(orderId));
            if (esResponseDTO.getTotal() > 0) {
                return esResponseDTO.getData().toJavaList(OrderPaymentDetailDO.class);
            }
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 查询订单金额
     *
     * @param orderId
     * @return
     */
    public List<OrderAmountDO> listOrderAmounts(String orderId) {
        try {
            EsResponseDTO esResponseDTO = esClientService.search(EsIndexNameEnum.ORDER_AMOUNT, buildSearchSourceBuilder(orderId));
            if (esResponseDTO.getTotal() > 0) {
                return esResponseDTO.getData().toJavaList(OrderAmountDO.class);
            }
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }


    private SearchSourceBuilder buildSearchSourceBuilder(String orderId) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery(OrderQueryFiledNameConstant.ORDER_ID, orderId));
        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }
}
