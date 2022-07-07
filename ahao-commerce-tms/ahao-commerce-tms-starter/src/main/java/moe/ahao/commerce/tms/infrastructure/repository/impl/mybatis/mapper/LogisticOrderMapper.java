package moe.ahao.commerce.tms.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.tms.infrastructure.repository.impl.mybatis.data.LogisticOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物流单 Mapper 接口
 */
@Mapper
public interface LogisticOrderMapper extends BaseMapper<LogisticOrderDO> {
    List<LogisticOrderDO> selectListByOrderId(@Param("orderId") String orderId);

    int deleteListByOrderId(@Param("orderId") String orderId);
}
