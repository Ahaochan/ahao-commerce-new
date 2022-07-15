package moe.ahao.commerce.customer.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum CustomerExceptionEnum {
    CUSTOMER_AUDIT_REPEAT(200000, "客服审核重复"),
    PROCESS_RECEIVE_AFTER_SALE(200001, "客服处理订单系统的售后信息失败"),
    PROCESS_RECEIVE_AFTER_SALE_REPEAT(200002, "客服处理订单系统的售后信息重复"),
    USER_ID_IS_NULL(200003, "用户ID不能为空"),
    ORDER_ID_IS_NULL(200004, "订单ID不能为空"),
    AFTER_SALE_ID_IS_NULL(200005, "售后ID不能为空"),
    AFTER_SALE_REFUND_ID_IS_NULL(200006, "售后支付ID不能为空"),
    AFTER_SALE_TYPE_IS_NULL(200007, "售后类型不能为空"),
    RETURN_GOOD_AMOUNT_IS_NULL(200008, "实际退款金额不能为空"),
    APPLY_REFUND_AMOUNT_IS_NULL(200009, "申请退款金额不能为空"),
    SAVE_AFTER_SALE_INFO_FAILED(200010, "保存售后信息失败, %s"),
    CUSTOMER_AUDIT_CANNOT_REPEAT(200011, "不能重复处理客服审核信息"),
    SEND_MQ_FAILED(200012, "发送MQ消息失败"),
    ;
    private final int code;
    private final String message;

    public CustomerException msg(Object... args) {
        return new CustomerException(code, String.format(message, args));
    }
}
