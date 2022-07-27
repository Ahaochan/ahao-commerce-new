package moe.ahao.commerce.order.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建订单返回结果
 */
@Data
public class CreateOrderDTO implements Serializable {
    /**
     * 订单ID
     */
    private String orderId;

    // 库存不足的商品列表
}
