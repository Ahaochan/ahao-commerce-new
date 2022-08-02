package com.ruyuan.eshop.order.elasticsearch.handler;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.domain.entity.OrderPaymentDetailDO;
import com.ruyuan.eshop.order.elasticsearch.entity.*;
import com.ruyuan.eshop.order.elasticsearch.query.AfterSaleListQueryIndex;
import com.ruyuan.eshop.order.elasticsearch.query.OrderListQueryIndex;

import java.util.List;

/**
 * es抽象handler
 * @author zhonghuashishan
 * @version 1.0
 */
public abstract class EsAbstractHandler {


    public void setEsIdOfOrderInfo(List<EsOrderInfo> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getOrderId()));
    }

    public void setEsIdOfOrderItem(List<EsOrderItem> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getOrderItemId()));
    }

    public void setEsIdOfOrderPaymentDetail(List<EsOrderPaymentDetail> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getOrderId()+"_"+e.getPayType()));
    }

    public void setEsIdOfOrderDeliveryDetail(List<EsOrderDeliveryDetail> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getOrderId()));
    }

    public void setEsIdOfOrderAmount(List<EsOrderAmount> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getOrderId()+"_"+e.getAmountType()));
    }

    public void setEsIdOfOrderAmountDetail(List<EsOrderAmountDetail> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getOrderId()+"_"+e.getSkuCode()+"_"+e.getAmountType()));
    }

    public void setEsIdOfAfterSaleInfo(List<EsAfterSaleInfo> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getAfterSaleId()));
    }

    public void setEsIdOfAfterSaleItem(List<EsAfterSaleItem> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getAfterSaleId()+"_"+e.getSkuCode()));
    }

    public void setEsIdOfAfterSaleRefund(List<EsAfterSaleRefund> list) {
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> e.setEsId(e.getAfterSaleId()));
    }


    public void setEsIdOfOrderListQueryIndex(List<OrderListQueryIndex> list) {
        list.forEach(e->e.setEsId(e.getOrderItemId()+"_"+e.getPayType()));
    }

    public void setEsIdOfAfterSaleListQueryIndex(List<AfterSaleListQueryIndex> list) {
        list.forEach(e->e.setEsId(e.getAfterSaleId()+"_"+e.getSkuCode()));
    }

}
