package moe.ahao.commerce.tms.infrastructure.gateway.impl.demo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceLogisticOrderDTO {
    /**
     * 三方物流单号
     */
    private String logisticCode;
    /**
     * 物流单内容
     */
    private String content;
}
