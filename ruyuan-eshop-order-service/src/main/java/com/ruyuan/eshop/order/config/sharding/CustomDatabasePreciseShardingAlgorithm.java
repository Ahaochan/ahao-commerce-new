package com.ruyuan.eshop.order.config.sharding;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * 自定义进准分库策略算法
 * @author zhonghuashishan
 * @version 1.0
 */
public class CustomDatabasePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {

    /**
     *
     * @param datasourceNames 数据源名称集合
     * @param preciseShardingValue 分片键信息
     * @return 匹配的数据源名
     */
    @Override
    public String doSharding(Collection<String> datasourceNames, PreciseShardingValue<String> preciseShardingValue) {
        return getDatabaseName(datasourceNames, preciseShardingValue.getValue());
    }

    /**
     * 获取数据源名
     * @param columnValue 分片键的值
     * @return 匹配的数据源名
     */
    private String getDatabaseName(Collection<String> dataSourceNames, String columnValue) {
        // 获取用户ID后三位
        String valueSuffix = (columnValue.length() < 3) ? columnValue : columnValue.substring(columnValue.length() - 3);
        // 计算将路由到的数据源名后缀
        String databaseSuffix = ShardingAlgorithmHelper.getDatabaseSuffix(Integer.parseInt(valueSuffix));
        for(String dataSourceName : dataSourceNames) {
            // 返回匹配到的真实数据源名
            if(dataSourceName.endsWith(databaseSuffix)) {
                return dataSourceName;
            }
        }
        return null;
    }
}