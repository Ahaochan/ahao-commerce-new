package moe.ahao.commerce.wms.api.command;

import lombok.Data;

/**
 * 取消捡货请求
 */
@Data
public class CancelPickGoodsCommand {
    /**
     * 订单id
     */
    private String orderId;
}
