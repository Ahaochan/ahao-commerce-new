package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public enum CustomerAuditResult {
    ACCEPT(1, "客服审核通过"),
    REJECT(2, "客服审核拒绝");
    private final Integer code;
    private final String name;
}
