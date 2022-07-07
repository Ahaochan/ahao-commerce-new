package moe.ahao.commerce.wms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 捡货结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickDTO {
    /**
     * 订单id
     */
    private String orderId;
}
