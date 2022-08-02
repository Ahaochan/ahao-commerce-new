package com.ruyuan.eshop.market.dao;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruyuan.eshop.common.dao.BaseDAO;
import com.ruyuan.eshop.market.domain.entity.MemberPointDO;
import com.ruyuan.eshop.market.mapper.MemberPointMapper;
import org.springframework.stereotype.Repository;

/**
 * 会员积分管理DAO组件
 *
 * @author zhonghuashishan
 */
@Repository
public class MemberPointDAO extends BaseDAO<MemberPointMapper, MemberPointDO> {

    /**
     * 根据userId查询会员积分
     *
     * @param userId
     * @return
     */
    public MemberPointDO getByUserId(String userId) {
        LambdaQueryWrapper<MemberPointDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberPointDO::getUserId, userId);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 添加用户积分
     *
     * @param userId
     * @param oldPoint
     * @param increasedPoint
     * @return
     */
    public boolean addUserPoint(String userId, Integer oldPoint, Integer increasedPoint) {
        LambdaUpdateWrapper<MemberPointDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(MemberPointDO::getPoint, oldPoint + increasedPoint)
                .eq(MemberPointDO::getUserId, userId)
                .eq(MemberPointDO::getPoint, oldPoint);
        ;
        return update(updateWrapper);
    }

}
