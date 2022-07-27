package moe.ahao.commerce.order.api.command;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 调整订单配送地址
 */
@Data
public class AdjustDeliveryAddressCommand {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 区
     */
    private String area;
    /**
     * 街道地址
     */
    private String street;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 经度
     */
    private BigDecimal lon;
    /**
     * 纬度
     */
    private BigDecimal lat;
}
