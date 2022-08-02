package com.ruyuan.eshop.order.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 生单发送超时未支付延迟消息入参
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class OrderPaymentDelayRequest implements Serializable {
    private static final long serialVersionUID = 1809218390180782239L;

    /**
     * 订单id
     */
    private String orderId;
}
