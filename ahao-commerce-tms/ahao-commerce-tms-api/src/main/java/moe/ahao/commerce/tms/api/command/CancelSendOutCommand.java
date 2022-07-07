package moe.ahao.commerce.tms.api.command;

import lombok.Data;

/**
 * 取消发货请求
 */
@Data
public class CancelSendOutCommand {
    /**
     * 订单id
     */
    private String orderId;
}
