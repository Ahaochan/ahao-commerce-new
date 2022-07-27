package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderOperateLogDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderOperateLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderOperateLogMybatisService extends ServiceImpl<OrderOperateLogMapper, OrderOperateLogDO> {
}
