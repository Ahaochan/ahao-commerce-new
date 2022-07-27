package moe.ahao.commerce.order.infrastructure.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleItemDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleRefundDO;

import java.util.List;

/**
 * 缺品数据
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLackInfo {

    /**
     * 缺品售后单
     */
    private AfterSaleInfoDO lackAfterSaleOrder;

    /**
     * 缺品售后单条目
     */
    private List<AfterSaleItemDO> afterSaleItems;

    /**
     * 售后退款单
     */
    private AfterSaleRefundDO afterSaleRefund;

    /**
     * 订单缺品扩展信息
     */
    private OrderExtJsonDTO lackExtJson;

    /**
     * 订单Id
     */
    private String orderId;

}
