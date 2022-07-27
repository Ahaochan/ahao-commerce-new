package moe.ahao.commerce.order.api.command;

import lombok.Data;

import java.util.Set;

/**
 * 移除订单的请求
 */
@Data
public class RemoveOrderCommand {
    /**
     * 要移除的订单ids
     */
    private Set<String> orderIds;
}
