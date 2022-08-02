package com.ruyuan.eshop.order.domain.request;

import com.ruyuan.eshop.order.enums.AfterSaleQueryDataTypeEnums;
import lombok.Data;

import java.io.Serializable;

/**
 * 售后单详情请求
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class AfterSaleDetailRequest implements Serializable {
    /**
     * 售后单id
     */
    private String afterSaleId;
    /**
     * 售后单项查询枚举
     */
    private AfterSaleQueryDataTypeEnums[] queryDataTypes;
}
