package moe.ahao.commerce.tms.api.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消发货请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelSendOutCommand {
    /**
     * 订单id
     */
    private String orderId;
}
