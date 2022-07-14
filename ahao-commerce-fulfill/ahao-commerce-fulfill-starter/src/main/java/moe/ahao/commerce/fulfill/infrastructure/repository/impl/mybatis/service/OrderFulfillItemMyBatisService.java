package moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.data.OrderFulfillItemDO;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.mapper.OrderFulfillItemMapper;
import org.springframework.stereotype.Service;

/**
 * 订单履约条目 Mapper 接口
 */
@Service
public class OrderFulfillItemMyBatisService extends ServiceImpl<OrderFulfillItemMapper, OrderFulfillItemDO> {
}
