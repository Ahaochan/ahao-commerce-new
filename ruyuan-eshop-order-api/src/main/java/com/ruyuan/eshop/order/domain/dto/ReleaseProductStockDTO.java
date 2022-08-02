package com.ruyuan.eshop.order.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 取消订单(全部条目退)和手动售后(单笔条目退)共用的释放库存DTO
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class ReleaseProductStockDTO implements Serializable {
    private static final long serialVersionUID = 4202385798844939307L;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单条目
     */
    private List<OrderItemRequest> orderItemRequestList;

    /**
     * skuCode 用于 手动售后
     */
    private String skuCode;

    /**
     * 用于 取消订单
     */
    @Data
    public static class OrderItemRequest implements Serializable {

        private static final long serialVersionUID = 6870559288334853954L;

        /**
         * 商品sku编号
         */
        private String skuCode;

        /**
         * 销售数量
         */
        private Integer saleQuantity;

    }

}