package moe.ahao.commerce.risk.api.command;

import lombok.Data;

/**
 * 订单风控检查入参
 */
@Data
public class CheckOrderRiskCommand {
    /**
     * 业务线标识
     */
    private Integer businessIdentifier;
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 卖家ID
     */
    private String sellerId;
    /**
     * 客户端ip
     */
    private String clientIp;
    /**
     * 设备标识
     */
    private String deviceId;
}
