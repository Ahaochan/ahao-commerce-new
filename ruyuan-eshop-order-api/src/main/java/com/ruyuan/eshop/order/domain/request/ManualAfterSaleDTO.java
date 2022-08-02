package com.ruyuan.eshop.order.domain.request;

import com.ruyuan.eshop.order.domain.dto.AfterSaleItemDTO;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.dto.OrderItemDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class ManualAfterSaleDTO implements Serializable {
    private static final long serialVersionUID = -5440307362822044532L;
    /**
     * sku编号
     */
    private String skuCode;

    /**
     * 退货数量
     */
    private Integer returnQuantity;

    /**
     * 实际退款金额
     */
    private Integer returnGoodAmount;

    /**
     * 申请退款金额
     */
    private Integer applyRefundAmount;

    /**
     * 商品总金额
     */
    private Integer originAmount;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单信息
     */
    private OrderInfoDTO orderInfoDTO;
    /**
     * 订单条目列表
     */
    private List<OrderItemDTO> orderItemDTOList;
    /**
     * 售后类型 1 整笔退款  2 售后退货
     */
    private Integer afterSaleType;
    /**
     * 售后id
     */
    private String afterSaleId;
    /**
     * 售后支付单id
     */
    private Long afterSaleRefundId;

    /**
     * 订单售后条目列表
     */
    private List<AfterSaleItemDTO> afterSaleItemDTOList;

}
