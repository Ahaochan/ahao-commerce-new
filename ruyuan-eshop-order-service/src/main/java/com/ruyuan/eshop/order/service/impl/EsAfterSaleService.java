package com.ruyuan.eshop.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.order.constants.AfterSaleQueryFiledNameConstant;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleRefundDO;
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
 * 售后单ES service
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
@Slf4j
public class EsAfterSaleService {

    @Autowired
    private EsClientService esClientService;

    /**
     * 查询售后单
     *
     * @param afterSaleId
     * @return
     */
    public AfterSaleInfoDO getAfterSale(String afterSaleId) {
        try {
            String result = esClientService.queryById(EsIndexNameEnum.AFTER_SALE_INFO, afterSaleId);
            return JSONObject.parseObject(result, AfterSaleInfoDO.class);
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 查询售后单条目
     *
     * @param afterSaleId
     * @return
     */
    public List<AfterSaleItemDO> listAfterSaleItems(String afterSaleId) {
        try {
            EsResponseDTO esResponseDTO = esClientService.search(EsIndexNameEnum.AFTER_SALE_ITEM, buildSearchSourceBuilder(afterSaleId));
            if (esResponseDTO.getTotal() > 0) {
                return esResponseDTO.getData().toJavaList(AfterSaleItemDO.class);
            }
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 查询售后退款单
     *
     * @param afterSaleId
     * @return
     */
    public List<AfterSaleRefundDO> listAfterSaleRefunds(String afterSaleId) {
        try {
            EsResponseDTO esResponseDTO = esClientService
                    .search(EsIndexNameEnum.AFTER_SALE_REFUND, buildSearchSourceBuilder(afterSaleId));
            if (esResponseDTO.getTotal() > 0) {
                return esResponseDTO.getData().toJavaList(AfterSaleRefundDO.class);
            }
        } catch (Exception e) {
            log.info("查询es异常，err={}", e.getMessage(), e);
        }
        return null;
    }

    private SearchSourceBuilder buildSearchSourceBuilder(String afterSaleId) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery(AfterSaleQueryFiledNameConstant.AFTER_SALE_ID, afterSaleId));
        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }
}
