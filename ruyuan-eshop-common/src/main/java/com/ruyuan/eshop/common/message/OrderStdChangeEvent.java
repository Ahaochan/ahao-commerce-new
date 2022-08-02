package com.ruyuan.eshop.common.message;

import com.ruyuan.eshop.common.enums.BusinessIdentifierEnum;
import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.enums.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单标准变更消息事件
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStdChangeEvent {

    /**
     * 接入方业务线标识  1, "自营商城"
     */
    private BusinessIdentifierEnum businessIdentifier;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 父订单编号
     */
    private String parentOrderId;

    /**
     * 接入方订单号
     */
    private String businessOrderId;

    /**
     * 订单类型 1:一般订单  255:其它
     */
    private OrderTypeEnum orderType;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 卖家编号
     */
    private String sellerId;

    /**
     * 交易总金额（以分为单位存储）
     */
    private Integer totalAmount;

    /**
     * 交易支付金额
     */
    private Integer payAmount;

    /**
     * 交易支付方式
     */
    private Integer payType;

    /**
     * 支付时间 yyyy-MM-dd HH:mm:ss
     */
    private String payTime;

    /**
     * 出库时间 yyyy-MM-dd HH:mm:ss
     */
    private String outStockTime;

    /**
     * 签收时间 yyyy-MM-dd HH:mm:ss
     */
    private String signedTime;

    /**
     * 订单状态变更枚举
     */
    private OrderStatusChangeEnum statusChange;
}
