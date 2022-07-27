package moe.ahao.commerce.aftersale.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后单状态变化枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleStatusChangeEnum {
    AFTER_SALE_REVOKE(AfterSaleStatusEnum.COMMITED, AfterSaleStatusEnum.REVOKE, "售后单撤销"),
    AFTER_SALE_REFUNDING(AfterSaleStatusEnum.REVIEW_PASS, AfterSaleStatusEnum.REFUNDING, "售后退款中"),
    AFTER_SALE_CUSTOMER_AUDIT_PASS(AfterSaleStatusEnum.COMMITED, AfterSaleStatusEnum.REVIEW_PASS, "客服审核通过"),
    AFTER_SALE_CUSTOMER_AUDIT_REJECT(AfterSaleStatusEnum.COMMITED, AfterSaleStatusEnum.REVIEW_REJECTED, "客服审核拒绝"),
    AFTER_SALE_PAYMENT_CALLBACK_PASS(AfterSaleStatusEnum.REFUNDING, AfterSaleStatusEnum.REFUNDED, "三方支付系统回调退款成功"),
    AFTER_SALE_PAYMENT_CALLBACK_FAILED(AfterSaleStatusEnum.REFUNDING, AfterSaleStatusEnum.FAILED, "三方支付系统回调退款失败"),
    ;
    private final AfterSaleStatusEnum preStatus;
    private final AfterSaleStatusEnum currentStatus;
    private final String operateRemark;


    public static AfterSaleStatusChangeEnum getBy(int preStatus, int currentStatus) {
        for (AfterSaleStatusChangeEnum element : AfterSaleStatusChangeEnum.values()) {
            if (preStatus == element.preStatus.getCode() && currentStatus == element.currentStatus.getCode()) {
                return element;
            }
        }
        return null;
    }
}
