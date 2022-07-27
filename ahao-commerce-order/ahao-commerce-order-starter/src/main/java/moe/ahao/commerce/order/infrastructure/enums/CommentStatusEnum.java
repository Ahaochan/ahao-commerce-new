package moe.ahao.commerce.order.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发表评论状态枚举
 */
@Getter
@AllArgsConstructor
public enum CommentStatusEnum {
    NO(0, "未删除"),
    YES(1, "已删除"),
    ;
    private final Integer code;
    private final String name;
}
