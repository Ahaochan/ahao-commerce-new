package com.ruyuan.eshop.order.converter;

import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.elasticsearch.entity.*;
import com.ruyuan.eshop.order.elasticsearch.query.AfterSaleListQueryIndex;
import com.ruyuan.eshop.order.elasticsearch.query.OrderListQueryIndex;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface EsEntityConverter {

    /**
     * 对象转换
     *
     * @param orderInfoDO 对象
     * @return 对象
     */
    EsOrderInfo convertToEsOrderInfo(OrderInfoDO orderInfoDO);

    /**
     * 对象转换
     *
     * @param orderInfoDOs 对象
     * @return 对象
     */
    List<EsOrderInfo> convertToEsOrderInfos(List<OrderInfoDO> orderInfoDOs);

    /**
     * 对象转换
     *
     * @param orderItemDO 对象
     * @return 对象
     */
    EsOrderItem convertToEsOrderItem(OrderItemDO orderItemDO);

    /**
     * 对象转换
     *
     * @param orderItemDOs 对象
     * @return 对象
     */
    List<EsOrderItem> convertToEsOrderItems(List<OrderItemDO> orderItemDOs);

    /**
     * 对象转换
     *
     * @param orderAmountDO 对象
     * @return 对象
     */
    EsOrderAmount convertToEsOrderAmount(OrderAmountDO orderAmountDO);

    /**
     * 对象转换
     *
     * @param orderAmountDOs 对象
     * @return 对象
     */
    List<EsOrderAmount> convertToEsOrderAmounts(List<OrderAmountDO> orderAmountDOs);

    /**
     * 对象转换
     *
     * @param orderAmountDetailDO 对象
     * @return 对象
     */
    EsOrderAmountDetail convertToEsOrderAmountDetail(OrderAmountDetailDO orderAmountDetailDO);

    /**
     * 对象转换
     *
     * @param orderAmountDetailDOs 对象
     * @return 对象
     */
    List<EsOrderAmountDetail> convertToEsOrderAmountDetails(List<OrderAmountDetailDO> orderAmountDetailDOs);

    /**
     * 对象转换
     *
     * @param orderDeliveryDetailDO 对象
     * @return 对象
     */
    EsOrderDeliveryDetail convertToEsOrderDeliveryDetail(OrderDeliveryDetailDO orderDeliveryDetailDO);

    /**
     * 对象转换
     *
     * @param orderDeliveryDetailDOs 对象
     * @return 对象
     */
    List<EsOrderDeliveryDetail> convertToEsOrderDeliveryDetails(List<OrderDeliveryDetailDO> orderDeliveryDetailDOs);

    /**
     * 对象转换
     *
     * @param orderPaymentDetailDO 对象
     * @return 对象
     */
    EsOrderPaymentDetail convertToEsOrderPaymentDetail(OrderPaymentDetailDO orderPaymentDetailDO);

    /**
     * 对象转换
     *
     * @param orderPaymentDetailDOs 对象
     * @return 对象
     */
    List<EsOrderPaymentDetail> convertToEsOrderPaymentDetails(List<OrderPaymentDetailDO> orderPaymentDetailDOs);

    /**
     * 对象转换
     *
     * @param queryIndex 对象
     * @return 对象
     */
    OrderListQueryIndex copyOrderListQueryIndex(OrderListQueryIndex queryIndex);


    /**
     * 对象转换
     *
     * @param afterSaleInfoDO 对象
     * @return 对象
     */
    EsAfterSaleInfo convertToEsAfterSaleInfo(AfterSaleInfoDO afterSaleInfoDO);

    /**
     * 对象转换
     *
     * @param afterSaleInfoDOs 对象
     * @return 对象
     */
    List<EsAfterSaleInfo> convertToEsAfterSaleInfos(List<AfterSaleInfoDO> afterSaleInfoDOs);

    /**
     * 对象转换
     *
     * @param afterSaleItemDO 对象
     * @return 对象
     */
    EsAfterSaleItem convertToEsAfterSaleItem(AfterSaleItemDO afterSaleItemDO);

    /**
     * 对象转换
     *
     * @param afterSaleItemDOs 对象
     * @return 对象
     */
    List<EsAfterSaleItem> convertToEsAfterSaleItems(List<AfterSaleItemDO> afterSaleItemDOs);

    /**
     * 对象转换
     *
     * @param afterSaleRefundDO 对象
     * @return 对象
     */
    EsAfterSaleRefund convertToEsAfterSaleRefund(AfterSaleRefundDO afterSaleRefundDO);

    /**
     * 对象转换
     *
     * @param afterSaleRefundDOs 对象
     * @return 对象
     */
    List<EsAfterSaleRefund> convertToEsAfterSaleRefunds(List<AfterSaleRefundDO> afterSaleRefundDOs);

    /**
     * 对象转换
     *
     * @param queryIndex 对象
     * @return 对象
     */
    AfterSaleListQueryIndex copyAfterSaleListQueryIndex(AfterSaleListQueryIndex queryIndex);
}
