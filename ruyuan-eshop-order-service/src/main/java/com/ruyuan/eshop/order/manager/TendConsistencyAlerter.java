package com.ruyuan.eshop.order.manager;

import com.ruyuan.consistency.custom.alerter.ConsistencyFrameworkAlerter;
import com.ruyuan.consistency.model.ConsistencyTaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 一致性框架告警器
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class TendConsistencyAlerter implements ConsistencyFrameworkAlerter {

    @Override
    public void sendAlertNotice(ConsistencyTaskInstance consistencyTaskInstance) {
        log.error("一致性任务执行失败，name={}, param={}", consistencyTaskInstance.getId(), consistencyTaskInstance.getTaskParameter());
    }
}
