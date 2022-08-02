package com.ruyuan.eshop.order.elasticsearch.entity;

import com.ruyuan.eshop.order.domain.entity.OrderDeliveryDetailDO;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsDocument;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsField;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsId;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import lombok.Data;

/**
 * es 订单配送信息
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@EsDocument(index = EsIndexNameEnum.ORDER_DELIVERY_DETAIL)
@Data
public class EsOrderDeliveryDetail extends OrderDeliveryDetailDO {
    /**
     * esId = orderId
     */
    @EsId
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String esId;
}
