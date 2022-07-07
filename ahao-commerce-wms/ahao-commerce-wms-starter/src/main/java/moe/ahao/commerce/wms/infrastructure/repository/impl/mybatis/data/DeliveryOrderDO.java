package moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;

/**
 * 出库单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_delivery_order")
public class DeliveryOrderDO extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 接入方业务线标识  1, "自营商城"
     */
    private Integer businessIdentifier;
    /**
     * 出库单ID
     */
    private String deliveryOrderId;
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 商家id
     */
    private String sellerId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 支付方式
     */
    private Integer payType;
    /**
     * 付款总金额
     */
    private BigDecimal payAmount;
    /**
     * 交易总金额
     */
    private BigDecimal totalAmount;
    /**
     * 运费
     */
    private BigDecimal deliveryAmount;
}
