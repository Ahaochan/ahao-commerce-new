package moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;

/**
 * 订单售后详情表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("after_sale_item")
public class AfterSaleItemDO extends BaseDO {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 售后id
     */
    private String afterSaleId;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * sku code
     */
    private String skuCode;
    /**
     * 商品名
     */
    private String productName;
    /**
     * 商品图片地址
     */
    private String productImg;
    /**
     * 商品退货数量
     */
    private BigDecimal returnQuantity;
    /**
     * 商品总金额
     */
    private BigDecimal originAmount;
    /**
     * 申请退款金额
     */
    private BigDecimal applyRefundAmount;
    /**
     * 实际退款金额
     */
    private BigDecimal realRefundAmount;
}
