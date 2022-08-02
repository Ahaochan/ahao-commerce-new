package com.ruyuan.eshop.order.domain.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付系统回调请求对象
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class PayCallbackRequest implements Serializable {

    private static final long serialVersionUID = 3685085492927992753L;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 支付账户
     */
    private String payAccount;

    /**
     * 支付金额
     */
    private Integer payAmount;

    /**
     * 支付系统交易单号
     */
    private String outTradeNo;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 支付时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /**
     * 商户号
     */
    private String merchantId;

    /**
     * 支付渠道
     */
    private String payChannel;

    /**
     * 微信平台 appid
     */
    private String appid;

}