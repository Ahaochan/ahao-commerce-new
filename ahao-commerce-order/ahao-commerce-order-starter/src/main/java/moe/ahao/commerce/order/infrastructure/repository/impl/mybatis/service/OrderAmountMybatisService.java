package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderAmountDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderAmountMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderAmountMybatisService extends ServiceImpl<OrderAmountMapper, OrderAmountDO> {
}
