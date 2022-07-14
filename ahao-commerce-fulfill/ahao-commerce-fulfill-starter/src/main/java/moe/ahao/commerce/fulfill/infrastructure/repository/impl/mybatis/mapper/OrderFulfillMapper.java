package moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.data.OrderFulfillDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单履约表 Mapper 接口
 */
@Mapper
public interface OrderFulfillMapper extends BaseMapper<OrderFulfillDO> {
    /**
     * 保存物流单号
     *
     * @param fulfillId     履约单id
     * @param logisticsCode 物流单号
     * @return 影响条数
     */
    int updateLogisticsCodeByFulfillId(@Param("fulfillId") String fulfillId, @Param("logisticsCode") String logisticsCode);

    /**
     * 更新配送员信息
     *
     * @param fulfillId      履约单id
     * @param delivererNo    配送单号
     * @param delivererName  配送员名称
     * @param delivererPhone 配送员电话
     * @return 影响条数
     */
    int updateDelivererInfoByFulfillId(@Param("fulfillId") String fulfillId, @Param("delivererNo") String delivererNo, @Param("delivererName") String delivererName, @Param("delivererPhone") String delivererPhone);

    /**
     * 查询履约单
     *
     * @param orderId 订单id
     * @return 履约单数据
     */
    OrderFulfillDO selectOneByOrderId(@Param("orderId") String orderId);
}
