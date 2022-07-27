package moe.ahao.commerce.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DO数据对象删除状态 0:未删除  1:已删除
 */
@Getter
@AllArgsConstructor
public enum DeleteStatusEnum {
    NO(0, "未删除"),
    YES(1, "已删除")
    ;
    private final Integer code;
    private final String name;
}
