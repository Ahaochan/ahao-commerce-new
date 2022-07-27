package moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 售后单变更表 Mapper 接口
 */
@Mapper
public interface AfterSaleLogMapper extends BaseMapper<AfterSaleLogDO> {
    /**
     * 根据售后单号查询售后单变更记录
     */
    List<AfterSaleLogDO> selectListByAfterSaleId(@Param("afterSaleId") String afterSaleId);
    /**
     * 根据售后单号查询售后单变更记录
     */
    AfterSaleLogDO selectOneByAfterSaleId(@Param("afterSaleId") String afterSaleId);
}
