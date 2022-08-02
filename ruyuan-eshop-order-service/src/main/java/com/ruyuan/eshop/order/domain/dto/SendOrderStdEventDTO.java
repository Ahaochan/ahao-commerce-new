package com.ruyuan.eshop.order.domain.dto;

import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendOrderStdEventDTO {

    /**
     * 订单状态变化枚举
     */
    private OrderStatusChangeEnum orderStatusChangeEnum;

    /**
     * 订单号
     */
    private String orderId;
}
