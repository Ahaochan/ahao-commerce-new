package com.ruyuan.eshop.order.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendLackItemRefundEventDTO implements Serializable {
    private static final long serialVersionUID = 33028405099378126L;

    /**
     * 售后id
     */
    private String afterSaleId;

    /**
     * 订单号
     */
    private String orderId;
}
