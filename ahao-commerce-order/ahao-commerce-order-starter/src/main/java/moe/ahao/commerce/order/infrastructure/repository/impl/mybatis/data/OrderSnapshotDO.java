package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import moe.ahao.domain.entity.BaseDO;

/**
 * 订单快照表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_snapshot")
@NoArgsConstructor
public class OrderSnapshotDO extends BaseDO {
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
     * 快照类型
     */
    private Integer snapshotType;
    /**
     * 订单快照内容
     */
    private String snapshotJson;

    public OrderSnapshotDO(OrderSnapshotDO that) {
        this.setId(that.id);
        this.setOrderId(that.orderId);
        this.setSnapshotType(that.snapshotType);
        this.setSnapshotJson(that.snapshotJson);
        this.setCreateBy(that.getCreateBy());
        this.setUpdateBy(that.getUpdateBy());
        this.setCreateTime(that.getCreateTime());
        this.setUpdateTime(that.getUpdateTime());
    }
}
