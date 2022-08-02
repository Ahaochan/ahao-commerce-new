package com.ruyuan.eshop.order.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户撤销售后申请
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class RevokeAfterSaleRequest implements Serializable {
    private static final long serialVersionUID = 8685567262789130599L;
    /**
     * 售后单
     */
    private String afterSaleId;
}
