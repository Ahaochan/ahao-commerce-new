package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderAmountDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderAmountDetailMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderAmountDetailMybatisService extends ServiceImpl<OrderAmountDetailMapper, OrderAmountDetailDO> {
}
