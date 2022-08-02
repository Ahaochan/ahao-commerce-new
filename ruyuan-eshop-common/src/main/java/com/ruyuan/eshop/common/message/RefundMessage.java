package com.ruyuan.eshop.common.message;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class RefundMessage implements Serializable {
    private static final long serialVersionUID = 8437194403570227792L;
    /**
     * 售后id
     */
    private String afterSaleId;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 售后的skuCode
     */
    private String skuCode;
    /**
     * 区分执行实际退款的消息类型: 1：取消订单整笔退款 or 2.发起售后退货
     */
    private Integer afterSaleType;

}
