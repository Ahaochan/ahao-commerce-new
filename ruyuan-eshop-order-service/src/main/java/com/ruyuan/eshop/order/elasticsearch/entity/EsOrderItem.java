package com.ruyuan.eshop.order.elasticsearch.entity;

import com.ruyuan.eshop.order.domain.entity.OrderItemDO;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsDocument;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsField;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsId;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import lombok.Data;

/**
 * es 订单条目
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@EsDocument(index = EsIndexNameEnum.ORDER_ITEM)
@Data
public class EsOrderItem extends OrderItemDO {
    /**
     * esId = orderItemId
     */
    @EsId
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String esId;
}
