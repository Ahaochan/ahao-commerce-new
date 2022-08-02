package com.ruyuan.eshop.order.elasticsearch.migrate;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 抽象迁移基类
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
public abstract class AbstractMigrateToEsHandler {

    public void execute(String content) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            doExecute(content);
        } catch (Exception e) {
            log.error("handler:{}，数据迁移至es异常:{} error={}", getHandlerName(), e.getMessage(), e);
            throw e;
        } finally {
            log.info("handler:{}，运行时长:{}ms", getHandlerName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    protected abstract void doExecute(String content) throws Exception;

    private String getHandlerName() {
        return this.getClass().getSimpleName();
    }
}
