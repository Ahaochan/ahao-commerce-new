package com.ruyuan.eshop.order.elasticsearch.query;

import com.alibaba.fastjson.annotation.JSONField;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsDocument;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsField;
import com.ruyuan.eshop.order.elasticsearch.annotation.EsId;
import com.ruyuan.eshop.order.elasticsearch.enums.EsDataTypeEnum;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * 售后单列表查询es index
 * 里面的属性来自于after_sale_info,after_sale_item,after_sale_refund
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@EsDocument(index = EsIndexNameEnum.AFTER_SALE_LIST_QUERY_INDEX)
@Data
@Builder
public class AfterSaleListQueryIndex {

    /**
     * 唯一id
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    @EsId
    private String esId;

    /**
     * 业务线
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer businessIdentifier;
    /**
     * 售后单号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String afterSaleId;
    /**
     * 订单号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderId;
    /**
     * 订单类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer orderType;
    /**
     * 售后单状态
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer afterSaleStatus;
    /**
     * 售后申请来源
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer applySource;
    /**
     * 售后类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer afterSaleType;

    /**
     * 用户ID
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String userId;
    /**
     * sku code
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String skuCode;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date createdTime;
    /**
     * 售后申请时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date applyTime;
    /**
     * 售后客服审核时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date reviewTime;
    /**
     * 退款支付时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date refundPayTime;
    /**
     * 退款金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer refundAmount;

    @Tolerate
    public AfterSaleListQueryIndex() {
    }

}
