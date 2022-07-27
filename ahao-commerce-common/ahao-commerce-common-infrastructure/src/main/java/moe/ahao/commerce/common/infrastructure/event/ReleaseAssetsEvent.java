package moe.ahao.commerce.common.infrastructure.event;

import lombok.Data;

/**
 * 取消订单释放资产
 */
@Data
public class ReleaseAssetsEvent {
    private String orderId;
}
