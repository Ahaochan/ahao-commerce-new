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
 * 订单列表查询es index
 * 里面的属性来自于order_info,order_item,order_delivery_detail,order_payment_detail
 * 订单搜索索引，4表合一，直接打了一个大宽表，避免了多表join关联，根本不要去搞join
 * 是没有全文索引，按照一个一个字段的值，去进行搜索
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@EsDocument(index = EsIndexNameEnum.ORDER_LIST_QUERY_INDEX)
@Data
@Builder
public class OrderListQueryIndex {

    /**
     * 唯一id，ES里一条数据主键值
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
     * 订单类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer orderType;
    /**
     * 订单号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderId;
    /**
     * 订单明细编号，面向搜索的索引，一条数据是订单条目这个粒度的
     * 每个订单条目，都在这个ES索引里有一一条数据
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String orderItemId;
    /**
     * 商品类型 1:普通商品,2:预售商品
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer productType;
    /**
     * 卖家ID
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String sellerId;
    /**
     * 父订单号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String parentOrderId;
    /**
     * 用户ID
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String userId;
    /**
     * 订单状态
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer orderStatus;
    /**
     * 收货人手机号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String receiverPhone;
    /**
     * 收货人姓名
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String receiverName;
    /**
     * 交易流水号
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String tradeNo;

    /**
     * sku code
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String skuCode;
    /**
     * sku商品名称
     */
    @EsField(type = EsDataTypeEnum.KEYWORD)
    private String productName;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date createdTime;
    /**
     * 支付时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @EsField(type = EsDataTypeEnum.DATE)
    private Date payTime;

    /**
     * 支付支付类型
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer payType;

    /**
     * 支付金额
     */
    @EsField(type = EsDataTypeEnum.INTEGER)
    private Integer payAmount;

    @Tolerate
    public OrderListQueryIndex() {
    }

}
