package com.ruyuan.eshop.order.config.sharding;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * 自定义精准分表策略算法
 * @author zhonghuashishan
 * @version 1.0
 */
public class CustomTablePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {

    /**
     * @param tableNames 表名集合
     * @param shardingValue 分片键信息
     * @return 匹配的表名集合
     */
    @Override
    public String doSharding(Collection<String> tableNames, PreciseShardingValue<String> shardingValue) {
        return getTableName(tableNames, shardingValue.getValue());
    }

    /**
     * 获取数据源名
     * @param columnValue 分片键信息
     * @return 匹配的表名
     */
    private String getTableName(Collection<String> tableNames, String columnValue) {
        // 获取用户ID后三位
        String valueSuffix = (columnValue.length() < 3) ? columnValue : columnValue.substring(columnValue.length() - 3);
        String tableSuffix = ShardingAlgorithmHelper.getTableSuffix(Integer.parseInt(valueSuffix));
        for(String tableName : tableNames) {
            if(tableName.endsWith(tableSuffix)) {
                return tableName;
            }
        }
        return null;
    }

}