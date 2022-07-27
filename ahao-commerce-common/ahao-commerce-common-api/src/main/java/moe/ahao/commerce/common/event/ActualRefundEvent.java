package moe.ahao.commerce.common.event;

import lombok.Data;

@Data
public class ActualRefundEvent {
    /**
     * 售后id
     */
    private String afterSaleId;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 当前订单是否是退最后一笔
     */
    private boolean lastReturnGoods = false;
}
