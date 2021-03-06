package moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("market_coupon_config")
public class CouponConfigDO extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 优惠券配置ID
     */
    private String couponConfigId;
    /**
     * 优惠券名称
     */
    private String name;
    /**
     * 优惠券类型，1：现金券，2：满减券
     */
    private Integer type;
    /**
     * 优惠券抵扣金额
     */
    private BigDecimal amount;
    /**
     * 优惠券使用限制金额
     */
    private BigDecimal conditionAmount;
    /**
     * 有效期开始时间
     */
    private Date validStartTime;
    /**
     * 有效期结束时间
     */
    private Date validEndTime;
    /**
     * 优惠券发行数量
     */
    private Long giveOutCount;
    /**
     * 优惠券已经领取的数量
     */
    private Long receivedCount;
    /**
     * 优惠券发放方式，1：可发放可领取，2：仅可发放，3：仅可领取
     */
    private Integer giveOutType;
    /**
     * 优惠券状态，1：未开始；2：发放中，3：已发完；4：已过期
     */
    private Integer status;
}
