package moe.ahao.commerce.aftersale.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单缺品信息DTO
 */
@Data
public class OrderLackItemDTO implements Serializable {
    /**
     * 售后信息
     */
    private AfterSaleInfoDTO afterSaleInfo;
    /**
     * 售后支付信息
     */
    private List<AfterSalePayDTO> afterSalePays;

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
