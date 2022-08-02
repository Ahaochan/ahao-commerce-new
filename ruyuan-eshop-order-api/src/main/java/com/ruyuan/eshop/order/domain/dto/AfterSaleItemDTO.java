package com.ruyuan.eshop.order.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 订单售后条目DTO
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
public class AfterSaleItemDTO implements Serializable {

    /**
     * 售后id
     */
    private String afterSaleId;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * sku code
     */
    private String skuCode;

    /**
     * 商品图片地址
     */
    private String productImg;

    /**
     * 商品退货数量
     */
    private Integer returnQuantity;

    /**
     * 商品总金额
     */
    private Integer originAmount;

    /**
     * 申请退款金额
     */
    private Integer applyRefundAmount;

    /**
     * 实际退款金额
     */
    private Integer realRefundAmount;

    /**
     * 商品名
     */
    private String productName;

    /**
     * 商品类型
     */
    private Integer productType;

    /**
     * 本条目退货完成标记 10:购买的sku未全部退货 20:购买的sku已全部退货
     */
    private Integer returnCompletionMark;

    /**
     * 售后条目类型 10:售后订单条目 20:尾笔条目退优惠券 30:尾笔条目退运费
     */
    private Integer afterSaleItemType;
}
