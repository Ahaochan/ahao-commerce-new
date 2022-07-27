package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import moe.ahao.domain.entity.BaseDO;

/**
 * 订单操作日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_operate_log")
@NoArgsConstructor
public class OrderOperateLogDO extends BaseDO {
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
     * 操作类型
     */
    private Integer operateType;
    /**
     * 前置状态
     */
    private Integer preStatus;
    /**
     * 当前状态
     */
    private Integer currentStatus;
    /**
     * 备注说明
     */
    private String remark;

    public OrderOperateLogDO(OrderOperateLogDO that) {
        this.setId(that.id);
        this.setOrderId(that.orderId);
        this.setOperateType(that.operateType);
        this.setPreStatus(that.preStatus);
        this.setCurrentStatus(that.currentStatus);
        this.setRemark(that.remark);
        this.setCreateBy(that.getCreateBy());
        this.setUpdateBy(that.getUpdateBy());
        this.setCreateTime(that.getCreateTime());
        this.setUpdateTime(that.getUpdateTime());
    }
}
