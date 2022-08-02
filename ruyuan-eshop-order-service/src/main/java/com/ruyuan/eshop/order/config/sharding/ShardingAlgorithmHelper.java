package com.ruyuan.eshop.order.config.sharding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 分片策略辅助组件
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class ShardingAlgorithmHelper {

    /**
     * 总的分库数量  默认8
     */
    protected static int DATABASE_SIZE;

    /**
     * 每个库的分表数量 默认64
     */
    protected static int TABLE_SIZE;

    @Value("${spring.shardingsphere.database.size:8}")
    public void setDatabaseSize(int databaseSize) {
        DATABASE_SIZE = databaseSize;
    }

    @Value("${spring.shardingsphere.table.size:64}")
    public void setTableSize(int tableSize) {
        TABLE_SIZE = tableSize;
    }

    /**
     * 计算database的后缀
     * @param valueSuffix 分片键的值后三位
     * @return 数据源名后缀
     */
    public static String getDatabaseSuffix(int valueSuffix) {
        return valueSuffix % DATABASE_SIZE + "";
    }

    /**
     * 计算table的后缀
     * @param valueSuffix 分片键的值后三位
     * @return 表名后缀
     */
    public static String getTableSuffix(int valueSuffix) {
        return valueSuffix / DATABASE_SIZE % TABLE_SIZE + "";
    }

}