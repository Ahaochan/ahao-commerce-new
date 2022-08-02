package com.ruyuan.eshop.product.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品预售信息
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreSaleInfoDTO implements Serializable {

    /**
     * 预售时间
     */
    private Date preSaleTime;

}
