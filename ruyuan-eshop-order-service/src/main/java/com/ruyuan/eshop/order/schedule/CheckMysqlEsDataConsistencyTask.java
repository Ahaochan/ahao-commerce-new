package com.ruyuan.eshop.order.schedule;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.dao.OrderInfoDAO;
import com.ruyuan.eshop.order.domain.dto.CheckOrderStatusConsistencyResultDTO;
import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 检测mysql中订单数据与es中的订单状态数据的一致性
 *
 * @author zhonghuashishan
 */
@Slf4j
@Component
public class CheckMysqlEsDataConsistencyTask {

    /**
     * es客户端组件
     */
    @Autowired
    private EsClientService esClientService;
    /**
     * 订单信息的DAO组件
     */
    @Autowired
    private OrderInfoDAO orderInfoDAO;

    /**
     * 检测mysql中订单数据与es中的订单状态数据的一致性
     */
    @XxlJob("checkMysqlEsDataConsistencyTask")
    public void checkMysqlEsDataConsistencyTask() throws Exception {

        // 查询出当前时间-25分钟 到 当前时间-5分钟 区间内的所有订单
        List<OrderInfoDO> orderInfoDOList = orderInfoDAO.list(buildQueryWrapper());

        if (CollectionUtils.isEmpty(orderInfoDOList)) {
            return;
        }

        // 检查数据库与es中的订单状态是否一致
        CheckOrderStatusConsistencyResultDTO checkOrderStatusConsistencyResultDTO = esClientService.checkOrderInfoDbAndEsDataConsistency(orderInfoDOList);
        // 如果检测结果为true且集合不为空
        if (checkOrderStatusConsistencyResultDTO.getResult() && CollectionUtils.isNotEmpty(checkOrderStatusConsistencyResultDTO.getDiffOrderInfos())) {
            boolean result = esClientService.batchSaveOrUpdate(checkOrderStatusConsistencyResultDTO.getDiffOrderInfos());
            log.info("完成es与数据库数据一致性任务的巡检校验操作，结果为[{}]", result);
        }

        XxlJobHelper.handleSuccess();
    }

    /**
     * 构造查询条件
     * @return 查询条件
     */
    private Wrapper<OrderInfoDO> buildQueryWrapper() {
        QueryWrapper<OrderInfoDO> queryWrapper = new QueryWrapper<>();
        Date startDate = getDateByDayNum(new Date(), Calendar.MINUTE, -25);
        Date endDate = getDateByDayNum(new Date(), Calendar.MINUTE, -5);
        queryWrapper.between("gmtCreate", startDate, endDate);
        return queryWrapper;
    }


    /**
     * 获得num天前的日期
     *
     * @param startDate    开始时间
     * @param calendarUnit 时间单位 用Calendar.DATE、Calendar.HOUR，Calendar.MINUTE 即可
     * @return num天前的日期
     */
    public static Date getDateByDayNum(Date startDate, int calendarUnit, int num) {
        if (num == 0) {
            return startDate;
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);
        calendar.add(calendarUnit, num);
        return calendar.getTime();
    }

}
