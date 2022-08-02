package com.ruyuan.eshop.order.domain.request;

import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 子订单创建请求
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubOrderPaidRequest {

    /**
     * 子订单
     */
    private OrderInfoDO subOrder;

    /**
     * 支付时间
     */
    private Date payTime;
}
