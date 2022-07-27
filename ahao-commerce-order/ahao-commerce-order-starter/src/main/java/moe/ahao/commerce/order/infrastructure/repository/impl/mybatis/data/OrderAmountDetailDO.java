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
 * 订单价格明细表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_amount_detail")
@NoArgsConstructor
public class OrderAmountDetailDO extends BaseDO {
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
     * 产品类型
     */
    private Integer productType;
    /**
     * 订单明细id
     */
    private String orderItemId;
    /**
     * 商品id
     */
    private String productId;
    /**
     * sku编码
     */
    private String skuCode;
    /**
     * 销售数量
     */
    private BigDecimal saleQuantity;
    /**
     * 销售单价
     */
    private BigDecimal salePrice;
    /**
     * 收费类型
     */
    private Integer amountType;
    /**
     * 收费金额
     */
    private BigDecimal amount;

    public OrderAmountDetailDO(OrderAmountDetailDO that) {
        this.setId(that.id);
        this.setOrderId(that.orderId);
        this.setProductType(that.productType);
        this.setOrderItemId(that.orderItemId);
        this.setProductId(that.productId);
        this.setSkuCode(that.skuCode);
        this.setSaleQuantity(that.saleQuantity);
        this.setSalePrice(that.salePrice);
        this.setAmountType(that.amountType);
        this.setAmount(that.amount);
        this.setCreateBy(that.getCreateBy());
        this.setUpdateBy(that.getUpdateBy());
        this.setCreateTime(that.getCreateTime());
        this.setUpdateTime(that.getUpdateTime());
    }
}
