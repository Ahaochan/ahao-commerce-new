package moe.ahao.commerce.customer.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;

/**
 * 客服接收售后申请信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_receives_after_sales_info")
public class CustomerReceivesAfterSaleInfoDO extends BaseDO {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 售后id
     */
    private String afterSaleId;
    /**
     * 售后支付单id
     */
    private String afterSaleRefundId;
    /**
     * 售后类型 1 退款  2 退货
     */
    private Integer afterSaleType;
    /**
     * 实际退款金额
     */
    private BigDecimal returnGoodAmount;
    /**
     * 申请退款金额
     */
    private BigDecimal applyRefundAmount;
}
