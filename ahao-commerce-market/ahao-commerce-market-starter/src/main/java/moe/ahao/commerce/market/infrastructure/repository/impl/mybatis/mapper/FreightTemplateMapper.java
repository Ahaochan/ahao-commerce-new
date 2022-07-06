package moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data.FreightTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 运费模板 Mapper 接口
 */
@Mapper
public interface FreightTemplateMapper extends BaseMapper<FreightTemplateDO> {
    /**
     * 通过区域ID查找运费模板
     */
    FreightTemplateDO selectOneByRegionId(@Param("regionId") String regionId);
}
