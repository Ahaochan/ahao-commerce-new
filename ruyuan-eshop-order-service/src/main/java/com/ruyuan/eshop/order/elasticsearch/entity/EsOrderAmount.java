package com.ruyuan.eshop.order.elasticsearch.entity;

import com.ruyuan.eshop.order.domain.entity.OrderAmountDO;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsDocument;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsField;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsId;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import lombok.Data;

/**
 * es 订单金额
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@EsDocument(index = EsIndexNameEnum.ORDER_AMOUNT)
@Data
public class EsOrderAmount extends OrderAmountDO {

    /**
     * esId = orderId + amountType
     */
    @EsId
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String esId;
}
