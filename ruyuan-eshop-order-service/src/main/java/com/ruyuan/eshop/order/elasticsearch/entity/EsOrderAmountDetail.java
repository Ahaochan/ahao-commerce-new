package com.ruyuan.eshop.order.elasticsearch.entity;

import com.ruyuan.eshop.order.domain.entity.OrderAmountDetailDO;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsDocument;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsField;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsId;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import lombok.Data;

/**
 * es 订单价格明细
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@EsDocument(index = EsIndexNameEnum.ORDER_AMOUNT_DETAIL)
@Data
public class EsOrderAmountDetail extends OrderAmountDetailDO {

    /**
     * esId = orderId + skuCode + amountType
     */
    @EsId
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String esId;

}
