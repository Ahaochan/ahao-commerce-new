package moe.ahao.commerce.order.infrastructure.domain.dto;

import lombok.Data;

/**
 * 订单扩展字段
 */
@Data
public class OrderExtJsonDTO {

    /**
     * 是否缺品 false:未缺品，true:缺品
     */
    private Boolean lackFlag = false;
    /**
     * 订单缺品信息
     */
    private OrderLackInfoDTO lackInfo;
}
