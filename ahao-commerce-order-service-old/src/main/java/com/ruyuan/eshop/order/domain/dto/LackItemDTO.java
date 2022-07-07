package com.ruyuan.eshop.order.domain.dto;

import com.ruyuan.eshop.order.domain.entity.OrderItemDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.ahao.commerce.product.api.dto.ProductSkuDTO;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LackItemDTO {

    /**
     * 缺品订单条目
     */
    private OrderItemDO orderItem;

    /**
     * 缺品数量
     */
    private BigDecimal lackNum;

    /**
     * 缺品商品sku
     */
    private ProductSkuDTO productSku;
}
