package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;

/**
 * 订单价格表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_amount")
@NoArgsConstructor
public class OrderAmountDO extends BaseDO {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 收费类型
     */
    private Integer amountType;
    /**
     * 收费金额
     */
    private BigDecimal amount;

    public OrderAmountDO(OrderAmountDO that) {
        this.setId(that.id);
        this.setOrderId(that.orderId);
        this.setAmountType(that.amountType);
        this.setAmount(that.amount);
        this.setCreateBy(that.getCreateBy());
        this.setUpdateBy(that.getUpdateBy());
        this.setCreateTime(that.getCreateTime());
        this.setUpdateTime(that.getUpdateTime());
    }
}
