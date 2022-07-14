package moe.ahao.commerce.common.api.event;

import lombok.Data;
import moe.ahao.commerce.common.enums.OrderStatusChangeEnum;

/**
 * 正向订单通用事件
 */
@Data
public class OrderEvent<T> {
    /**
     * 接入方业务线标识  1, "自营商城"
     */
    private Integer businessIdentifier;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单类型 1:一般订单  255:其它
     */
    private Integer orderType;
    /**
     * 卖家编号
     */
    private String sellerId;
    /**
     * 订单变更事件
     */
    private OrderStatusChangeEnum orderStatusChange;
    /**
     * 消息体
     */
    private T messageContent;
}
