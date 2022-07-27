package moe.ahao.commerce.order.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PrePayOrderDTO {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 支付系统交易单号
     */
    private String outTradeNo;
    /**
     * 支付方式
     */
    private Integer payType;
    /**
     * 支付系统返回的信息
     * 不同的字符方式，返回的内容会不一样
     */
    private Map<String, Object> payData;
}
