package moe.ahao.commerce.customer.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckReceiveCustomerAuditResultDTO {
    /**
     * 风控检查结果
     */
    private Boolean result;

    /**
     * 风控提示信息
     */
    private List<String> noticeList;
}
