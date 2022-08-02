package com.ruyuan.eshop.order.schedule;

import com.ruyuan.consistency.manager.TaskScheduleManager;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 一致性框架执行逻辑
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class TendConsistencyXxlTask {

    /**
     * 一致性任务调度器
     */
    @Resource
    private TaskScheduleManager taskScheduleManager;

    // 在xxl-admin配置每5秒执行一次，频率高一点
    @XxlJob("consistencyRetryTask")
    public void execute() {
        try {
            taskScheduleManager.performanceTask();
        } catch (Exception e) {
            log.error("一致性任务调度时，发送异常", e);
        }
        XxlJobHelper.handleSuccess();
    }

}
