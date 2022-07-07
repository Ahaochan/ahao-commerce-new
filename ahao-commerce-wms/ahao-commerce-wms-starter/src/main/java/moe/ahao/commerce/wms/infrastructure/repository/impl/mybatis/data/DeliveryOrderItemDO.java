package moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;

/**
 * 出库单条目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_delivery_order_item")
public class DeliveryOrderItemDO extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 出库单ID
     */
    private String deliveryOrderId;
    /**
     * 商品sku
     */
    private String skuCode;
    /**
     * 商品名称
     */
    private String productName;
    /**
     * 销售单价
     */
    private BigDecimal salePrice;
    /**
     * 销售数量
     */
    private BigDecimal saleQuantity;
    /**
     * 商品单位
     */
    private String productUnit;
    /**
     * 付款金额
     */
    private BigDecimal payAmount;
    /**
     * 当前商品支付原总价
     */
    private BigDecimal originAmount;
    /**
     * 拣货数量
     */
    private BigDecimal pickingCount;
    /**
     * 捡货仓库货柜ID
     */
    private String skuContainerId;
}
