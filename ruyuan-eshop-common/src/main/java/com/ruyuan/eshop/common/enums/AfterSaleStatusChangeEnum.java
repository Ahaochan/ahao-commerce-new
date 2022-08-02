package com.ruyuan.eshop.common.enums;

import lombok.Getter;

/**
 * 售后单状态变化枚举
 */
@Getter
public enum AfterSaleStatusChangeEnum {

    //售后单已创建
    AFTER_SALE_CREATED(AfterSaleStatusEnum.UN_CREATED, AfterSaleStatusEnum.COMMITTED, AfterSaleOperateTypeEnum.NEW_AFTER_SALE),

    //缺品售后单已创建
    LACK_AFTER_SALE_CREATED(AfterSaleStatusEnum.UN_CREATED, AfterSaleStatusEnum.REVIEW_PASS, AfterSaleOperateTypeEnum.NEW_LACK_AFTER_SALE),

    //售后单已审核通过
    AFTER_SALE_REVIEWED_PASS(AfterSaleStatusEnum.COMMITTED, AfterSaleStatusEnum.REVIEW_PASS, AfterSaleOperateTypeEnum.REVIEW_AFTER_SALE_PASS),

    //售后单已审核拒绝
    AFTER_SALE_REVIEWED_REJECTION(AfterSaleStatusEnum.COMMITTED, AfterSaleStatusEnum.REVIEW_REJECTED, AfterSaleOperateTypeEnum.REVIEW_AFTER_SALE_REJECTION),

    //售后单已撤销
    AFTER_SALE_REVOKED(AfterSaleStatusEnum.COMMITTED, AfterSaleStatusEnum.REVOKE, AfterSaleOperateTypeEnum.REVOKE_AFTER_SALE),

    //售后单退款中
    AFTER_SALE_REFUNDING(AfterSaleStatusEnum.REVIEW_PASS, AfterSaleStatusEnum.REFUNDING, AfterSaleOperateTypeEnum.AFTER_SALE_REFUNDING),

    //售后单退款成功
    AFTER_SALE_REFUNDED(AfterSaleStatusEnum.REFUNDING, AfterSaleStatusEnum.REFUNDED, AfterSaleOperateTypeEnum.AFTER_SALE_REFUNDED),

    //售后单退款失败
    AFTER_SALE_REFUND_FAILED(AfterSaleStatusEnum.REFUNDING, AfterSaleStatusEnum.FAILED, AfterSaleOperateTypeEnum.AFTER_SALE_REFUND_FAIL),
    ;


    AfterSaleStatusChangeEnum(AfterSaleStatusEnum preStatus, AfterSaleStatusEnum currentStatus
            , AfterSaleOperateTypeEnum operateType) {
        this.preStatus = preStatus;
        this.currentStatus = currentStatus;
        this.operateType = operateType;
    }

    private AfterSaleStatusEnum preStatus;
    private AfterSaleStatusEnum currentStatus;
    private AfterSaleOperateTypeEnum operateType;


    public static AfterSaleStatusChangeEnum getBy(int preStatus, int currentStatus) {
        for (AfterSaleStatusChangeEnum element : AfterSaleStatusChangeEnum.values()) {
            if (preStatus == element.preStatus.getCode() &&
                    currentStatus == element.currentStatus.getCode()) {
                return element;
            }
        }
        return null;
    }
}
