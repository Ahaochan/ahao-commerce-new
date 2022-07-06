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
@TableName("market_coupon")
public class CouponDO extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 优惠券ID
     */
    private String couponId;
    /**
     * 优惠券配置ID
     */
    private String couponConfigId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 是否使用过这个优惠券，1：使用了，0：未使用
     */
    private Integer used;
    /**
     * 使用优惠券的时间
     */
    private Date usedTime;
    /**
     * 抵扣金额
     */
    private BigDecimal amount;
}
