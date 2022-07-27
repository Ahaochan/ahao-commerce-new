package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客服审核来源枚举
 */
@Getter
@AllArgsConstructor
public enum CustomerAuditSourceEnum {
    SELF_MALL(1, "自营商城");
    private final Integer code;
    private final String name;
}
