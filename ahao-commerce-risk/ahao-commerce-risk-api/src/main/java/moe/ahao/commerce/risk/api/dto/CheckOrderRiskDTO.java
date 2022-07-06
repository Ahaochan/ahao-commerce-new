package moe.ahao.commerce.risk.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 订单风控检查结果
 */
@Data
public class CheckOrderRiskDTO {
    /**
     * 风控检查结果
     */
    private Boolean result;
    /**
     * 风控提示信息
     */
    private List<String> noticeList;
}
