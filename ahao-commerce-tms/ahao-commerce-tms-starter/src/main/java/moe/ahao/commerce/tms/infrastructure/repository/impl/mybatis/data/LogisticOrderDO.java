package moe.ahao.commerce.tms.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ahao.domain.entity.BaseDO;

/**
 * 物流单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_logistic_order")
public class LogisticOrderDO extends BaseDO {
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
     * 订单号
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
     * 物流单号
     */
    private String logisticCode;
    /**
     * 物流单内容
     */
    private String content;
}
