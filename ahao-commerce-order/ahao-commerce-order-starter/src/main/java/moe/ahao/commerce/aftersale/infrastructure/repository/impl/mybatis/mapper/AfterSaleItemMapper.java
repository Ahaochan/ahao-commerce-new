package moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单售后条目表 Mapper 接口
 */
@Mapper
public interface AfterSaleItemMapper extends BaseMapper<AfterSaleItemDO> {
    /**
     * 根据售后单号查询售后单条目记录
     */
    List<AfterSaleItemDO> selectListByAfterSaleId(@Param("afterSaleId") String afterSaleId);
    /**
     * 根据订单号查询售后单条目
     */
    List<AfterSaleItemDO> selectListByOrderId(@Param("orderId") String orderId);
    /**
     * 根据orderId和skuCode查询售后单条目
     * 这里做成list便于以后扩展
     * 目前仅支持整笔条目的退货，所以当前list里只有一条
     */
    List<AfterSaleItemDO> selectListByOrderIdAndSkuCode(@Param("orderId") String orderId, @Param("skuCode") String skuCode);

    /**
     * 查询出不包含当前afterSaleId的售后条目
     */
    List<AfterSaleItemDO> selectListByOrderIdAndExcludeAfterSaleId(@Param("orderId") String orderId, @Param("afterSaleId") String afterSaleId);
}
