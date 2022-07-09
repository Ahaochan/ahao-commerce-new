package moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.inventory.infrastructure.repository.impl.mybatis.data.ProductStockLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 库存扣减日志表 Mapper 接口
 */
@Mapper
public interface ProductStockLogMapper extends BaseMapper<ProductStockLogDO> {
    void updateStatusById(@Param("id") Long id, @Param("status") Integer status);

    ProductStockLogDO selectOneByOrderIdAndSkuCode(@Param("order_id") String orderId, @Param("skuCode") String skuCode);
    ProductStockLogDO selectLastOneBySkuCode(@Param("skuCode") String skuCode);
}
