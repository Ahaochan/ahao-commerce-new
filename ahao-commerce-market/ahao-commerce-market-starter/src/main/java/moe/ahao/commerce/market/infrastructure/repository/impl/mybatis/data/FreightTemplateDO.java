package moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("market_freight_template")
public class FreightTemplateDO extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 区域ID
     */
    private String regionId;
    /**
     * 标准运费
     */
    private BigDecimal shippingAmount;
    /**
     * 订单满多少钱则免运费
     */
    private BigDecimal conditionAmount;
}
