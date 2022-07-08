package moe.ahao.commerce.pay.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * 预支付订单返回结果
 */
@Data
public class PayOrderDTO {
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
     * 第三方支付平台的支付数据
     */
    private Map<String, Object> payData;
}
