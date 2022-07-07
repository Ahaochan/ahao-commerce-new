package moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data.DeliveryOrderItemDO;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.mapper.DeliveryOrderItemMapper;
import org.springframework.stereotype.Service;

@Service
public class DeliveryOrderItemMybatisService extends ServiceImpl<DeliveryOrderItemMapper, DeliveryOrderItemDO> {
}
