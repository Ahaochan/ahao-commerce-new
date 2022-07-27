package moe.ahao.commerce.pay.api.command;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PayOrderCommand {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 业务方标识
     */
    private Integer businessIdentifier;
    /**
     * 支付类型
     */
    private Integer payType;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单支付金额
     */
    private BigDecimal payAmount;
    /**
     * 支付成功后跳转地址
     */
    private String callbackUrl;
    /**
     * 支付失败跳转地址
     */
    private String callbackFailUrl;
    /**
     * 微信openid
     */
    private String openid;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 订单摘要
     */
    private String subject;
    /**
     * 商品明细 json
     */
    private String itemInfo;
}
