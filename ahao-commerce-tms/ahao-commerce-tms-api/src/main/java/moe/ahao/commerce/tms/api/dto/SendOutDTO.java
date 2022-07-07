package moe.ahao.commerce.tms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 发货结果
 */
@Data
@AllArgsConstructor
public class SendOutDTO {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 物流单号
     */
    private String logisticsCode;
}
