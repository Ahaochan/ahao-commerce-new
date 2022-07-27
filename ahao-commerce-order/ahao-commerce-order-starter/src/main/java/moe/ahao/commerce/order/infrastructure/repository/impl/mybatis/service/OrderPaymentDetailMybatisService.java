package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderPaymentDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderPaymentDetailMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderPaymentDetailMybatisService extends ServiceImpl<OrderPaymentDetailMapper, OrderPaymentDetailDO> {
}
