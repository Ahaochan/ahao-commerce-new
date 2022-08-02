package com.ruyuan.eshop.order.domain.request;

import com.ruyuan.eshop.order.builder.FullOrderData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子订单创建请求
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubOrderCreateRequest {

    /**
     * 主单信息
     */
    private FullOrderData fullMasterOrderData;

    /**
     * 商品类型
     */
    private Integer productType;
}
