package com.ruyuan.eshop.order.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 售后单详情DTO
 * </p>
 *
 * @author zhonghuashishan
 */
@Data
@Builder
public class AfterSaleOrderDetailDTO implements Serializable {

    /**
     * 售后信息
     */
    private AfterSaleInfoDTO afterSaleInfo;

    /**
     * 售后单条目
     */
    private List<AfterSaleItemDTO> afterSaleItems;

    /**
     * 售后退款信息
     */
    private List<AfterSaleRefundDTO> afterSaleRefunds;

    /**
     * 售后单日志
     */
    private List<AfterSaleLogDTO> afterSaleLogs;
}
