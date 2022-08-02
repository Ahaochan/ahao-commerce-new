package com.ruyuan.eshop.order.elasticsearch.entity;

import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsDocument;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsField;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsId;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import lombok.Data;

/**
 * es 订单
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@EsDocument(index = EsIndexNameEnum.ORDER_INFO)
@Data
public class EsOrderInfo extends OrderInfoDO {
    /**
     * esId = orderId
     */
    @EsId
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String esId;
}
