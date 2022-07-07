package moe.ahao.commerce.product.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.product.infrastructure.repository.impl.mybatis.data.ProductSkuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品sku记录表 Mapper 接口
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSkuDO> {
    ProductSkuDO selectOneBySkuCode(@Param("skuCode") String skuCode);
    List<ProductSkuDO> selectListBySkuCodeList(@Param("skuCodeList") List<String> skuCodeList);
}
