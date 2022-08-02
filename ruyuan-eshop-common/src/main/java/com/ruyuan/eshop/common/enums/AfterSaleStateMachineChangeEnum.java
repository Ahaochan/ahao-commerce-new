package com.ruyuan.eshop.common.enums;

import lombok.Getter;

/**
 * 售后状态机状态变化枚举
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Getter
public enum AfterSaleStateMachineChangeEnum {
    //  发起售后    0 未创建 -> 10 提交申请
    INITIATE_AFTER_SALE(AfterSaleStatusEnum.UN_CREATED, AfterSaleStatusEnum.COMMITTED, "initiate_after_sale"),

    //  客服审核通过   10 提交申请 -> 20 审核通过
    AUDIT_PASS(AfterSaleStatusEnum.COMMITTED, AfterSaleStatusEnum.REVIEW_PASS, "audit_pass"),

    //  客服审核拒绝   10 提交申请 -> 30 审核拒绝
    AUDIT_REJECT(AfterSaleStatusEnum.COMMITTED, AfterSaleStatusEnum.REVIEW_REJECTED, "audit_reject"),

    //  退款中    20 审核通过 -> 40 退款中
    REFUNDING(AfterSaleStatusEnum.REVIEW_PASS, AfterSaleStatusEnum.REFUNDING, "refunding"),

    //  默认 退款成功  40 退款中 -> 50 退款成功 如果退款失败，在状态机内部有流转记录
    REFUND_DEFAULT(AfterSaleStatusEnum.REFUNDING, AfterSaleStatusEnum.REFUNDED, "refund_success"),

    //  取消订单    0 未创建 -> 20 审核通过
    CANCEL_ORDER(AfterSaleStatusEnum.UN_CREATED, AfterSaleStatusEnum.REVIEW_PASS, "cancel_order"),

    //  撤销售后  10 提交申请 -> 127 撤销申请
    REVOKE_AFTER_SALE(AfterSaleStatusEnum.COMMITTED, AfterSaleStatusEnum.REVOKE, "revoke_after_sale"),
    ;

    AfterSaleStateMachineChangeEnum(AfterSaleStatusEnum fromStatus, AfterSaleStatusEnum toStatus, String tags) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.tags = tags;
    }

    private AfterSaleStatusEnum fromStatus;
    private AfterSaleStatusEnum toStatus;
    private String tags;

    public void setToStatus(AfterSaleStatusEnum toStatus) {
        this.toStatus = toStatus;
    }
}
