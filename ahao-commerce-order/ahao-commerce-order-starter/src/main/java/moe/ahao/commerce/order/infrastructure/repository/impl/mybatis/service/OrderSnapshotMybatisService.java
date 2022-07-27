package moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderSnapshotDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderSnapshotMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OrderSnapshotMybatisService extends ServiceImpl<OrderSnapshotMapper, OrderSnapshotDO> {
}
