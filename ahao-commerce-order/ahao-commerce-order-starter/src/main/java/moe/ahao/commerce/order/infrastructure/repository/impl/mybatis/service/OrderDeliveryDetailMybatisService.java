package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderDeliveryDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderDeliveryDetailMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDeliveryDetailMybatisService extends ServiceImpl<OrderDeliveryDetailMapper, OrderDeliveryDetailDO> {
}
