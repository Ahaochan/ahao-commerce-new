package com.ruyuan.eshop.fulfill.schedule;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.common.utils.ExtJsonUtil;
import com.ruyuan.eshop.fulfill.dao.OrderFulfillDAO;
import com.ruyuan.eshop.fulfill.dao.OrderFulfillItemDAO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillItemDO;
import com.ruyuan.eshop.fulfill.enums.OrderFulfillTypeEnum;
import com.ruyuan.eshop.fulfill.service.impl.OrderFulfillScheduleService;
import com.ruyuan.eshop.product.domain.dto.PreSaleInfoDTO;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 触发预售商品履约的定时任务
 *
 * @author zhonghuashishan
 */
@Slf4j
@Component
public class PreSaleOrderFulfillScheduleTriggerTask {

    @Autowired
    private OrderFulfillDAO orderFulfillDAO;

    @Autowired
    private OrderFulfillItemDAO orderFulfillItemDAO;

    @Autowired
    private OrderFulfillScheduleService orderFulfillScheduleService;

    /**
     * 执行任务逻辑
     */
    @XxlJob("preSaleOrderFulfillScheduleTriggerTask")
    public void execute() {
        // 1、查询未履约预售单的候选集
        // 真实的生产环境，还需要加上截止时间进行过滤，具体可以参考订单系统取消订单定时任务
        List<OrderFulfillDO> list = orderFulfillDAO.getNotFulfillCandidates(OrderFulfillTypeEnum.PRE_SALE.getCode());
        log.info("查询未履约预售单的候选集: list={}", JSONObject.toJSONString(list));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (OrderFulfillDO orderFulfill : list) {
            try {
                PreSaleInfoDTO preSaleInfoDTO = ExtJsonUtil.parseExtJson(orderFulfill.getExtJson(), PreSaleInfoDTO.class);
                if (new Date().getTime() >= preSaleInfoDTO.getPreSaleTime().getTime()) {
                    // 2、触发预售商品履约调度
                    List<OrderFulfillItemDO> orderFulfillItems = orderFulfillItemDAO.listByFulfillId(orderFulfill.getFulfillId());
                    orderFulfillScheduleService.doSchedule(orderFulfill, orderFulfillItems);
                }
            } catch (Exception e) {
                log.error("触发预售商品履约调度失败，err={}", e.getMessage(), e);
            }
        }

        XxlJobHelper.handleSuccess();
    }


}
