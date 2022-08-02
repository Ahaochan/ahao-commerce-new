package com.ruyuan.eshop.inventory.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 同步商品sku库存数据到缓存
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncStockToCacheRequest implements Serializable {

    /**
     * 商品sku编号
     */
    private String skuCode;
}
