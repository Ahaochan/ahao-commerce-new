package moe.ahao.commerce.aftersale.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单售后DTO
 */
@Data
public class AfterSaleInfoDTO {
    /**
     * 订单实付金额
     */
    private Integer orderAmount;
    /**
     * 订单状态
     */
    private Integer orderStatus;
    /**
     * 售后id
     */
    private String afterSaleId;
    /**
     * 接入方业务标识
     */
    private Integer businessIdentifier;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 购买用户id
     */
    private String userId;
    /**
     * 订单类型
     */
    private Integer orderType;
    /**
     * 申请售后来源
     */
    private Integer applySource;
    /**
     * 申请售后时间
     */
    private Date applyTime;
    /**
     * 申请原因编码
     */
    private Integer applyReasonCode;
    /**
     * 申请原因
     */
    private String applyReason;
    /**
     * 审核时间
     */
    private Date reviewTime;
    /**
     * 客服审核来源
     */
    private Integer reviewSource;
    /**
     * 客服审核结果编码
     */
    private Integer reviewReasonCode;
    /**
     * 客服审核结果
     */
    private String reviewReason;
    /**
     * 售后类型：1、退货 2、退款
     */
    private Integer afterSaleType;
    /**
     * 售后类型详情枚举
     */
    private Integer afterSaleTypeDetail;
    /**
     * 售后单状态
     */
    private Integer afterSaleStatus;
    /**
     * 申请退款金额
     */
    private BigDecimal applyRefundAmount;
    /**
     * 实际退款金额
     */
    private BigDecimal realRefundAmount;
    /**
     * 备注
     */
    private String remark;

    /**
     * 售后单条目
     */
    private List<AfterSaleItemDTO> afterSaleItems;

    /**
     * 订单售后条目DTO
     */
    @Data
    public static class AfterSaleItemDTO {
        /**
         * 售后id
         */
        private String afterSaleId;
        /**
         * 订单id
         */
        private String orderId;
        /**
         * sku code
         */
        private String skuCode;
        /**
         * 商品名
         */
        private String productName;
        /**
         * 商品图片地址
         */
        private String productImg;
        /**
         * 商品退货数量
         */
        private BigDecimal returnQuantity;
        /**
         * 商品总金额
         */
        private BigDecimal originAmount;
        /**
         * 申请退款金额
         */
        private BigDecimal applyRefundAmount;
        /**
         * 实际退款金额
         */
        private BigDecimal realRefundAmount;

        /**
         * 商品类型
         */
        private Integer productType;
        /**
         * 商品退货数量
         */
        private Integer productNum;
    }

    /**
     * 售后支付表
     */
    @Data
    public static class AfterSalePayDTO {
        /**
         * 售后单id
         */
        private String afterSaleId;
        /**
         * 订单id
         */
        private String orderId;
        /**
         * 售后批次编号
         */
        private String afterSaleBatchNo;
        /**
         * 账户类型
         */
        private Integer accountType;
        /**
         * 支付类型
         */
        private Integer payType;
        /**
         * 售后单状态
         */
        private Integer afterSaleStatus;
        /**
         * 退款金额
         */
        private Integer refundAmount;
        /**
         * 退款支付时间
         */
        private Date refundPayTime;
        /**
         * 交易单号
         */
        private String outTradeNo;
        /**
         * 备注
         */
        private String remark;
    }
}
