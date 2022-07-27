package moe.ahao.commerce.common.infrastructure.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单完成支付消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaidOrderSuccessEvent {
    private String orderId;
}
