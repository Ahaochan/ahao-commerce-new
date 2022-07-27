package moe.ahao.commerce.aftersale.api.command;

import lombok.Data;

/**
 * 用户撤销售后申请
 */
@Data
public class RevokeAfterSaleCommand {
    /**
     * 售后单id
     */
    private String afterSaleId;
}
