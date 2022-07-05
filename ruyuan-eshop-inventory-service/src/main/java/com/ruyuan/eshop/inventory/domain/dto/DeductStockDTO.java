package com.ruyuan.eshop.inventory.domain.dto;

import com.ruyuan.eshop.inventory.domain.entity.ProductStockDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扣减库存DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeductStockDTO {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 商品skuCode
     */
    private String skuCode;

    /**
     * 销售数量
     */
    private Integer saleQuantity;

    /**
     * sku库存数据
     */
    private ProductStockDO productStockDO;
}
