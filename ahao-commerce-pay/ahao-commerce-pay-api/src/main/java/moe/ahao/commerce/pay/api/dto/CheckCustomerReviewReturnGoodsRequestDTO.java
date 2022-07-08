package moe.ahao.commerce.pay.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckCustomerReviewReturnGoodsRequestDTO {
    /**
     * 风控检查结果
     */
    private Boolean result;
    /**
     * 风控提示信息
     */
    private List<String> noticeList;
}
