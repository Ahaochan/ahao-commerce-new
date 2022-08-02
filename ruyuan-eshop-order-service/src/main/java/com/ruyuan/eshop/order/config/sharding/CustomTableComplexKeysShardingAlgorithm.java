package com.ruyuan.eshop.order.config.sharding;

import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义复合分表策略算法
 * @author zhonghuashishan
 * @version 1.0
 */
@NoArgsConstructor
public class CustomTableComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<String> {

    /**
     * 分片键优先级依次为：
     * order_id、user_id、after_sale_id、parent_order_id
     *
     * 虽然是多字段路由，但最后都是取的userId的后三位
     *
     * @param tableNames 表名集合
     * @param shardingValue 分片键信息
     * @return 匹配的表名集合
     */
    @Override
    public Collection<String> doSharding(Collection<String> tableNames,
                                         ComplexKeysShardingValue<String> shardingValue) {
        Collection<String> parentOrderIds = shardingValue.getColumnNameAndShardingValuesMap().get("parent_order_id");
        Collection<String> orderIds = shardingValue.getColumnNameAndShardingValuesMap().get("order_id");
        Collection<String> userIds = shardingValue.getColumnNameAndShardingValuesMap().get("user_id");
        Collection<String> afterSaleIds = shardingValue.getColumnNameAndShardingValuesMap().get("after_sale_id");

        if (CollectionUtils.isNotEmpty(orderIds)) {
            return getTableNames(tableNames, orderIds);
        }

        if (CollectionUtils.isNotEmpty(userIds)) {
            return getTableNames(tableNames, userIds);
        }

        if (CollectionUtils.isNotEmpty(afterSaleIds)) {
            return getTableNames(tableNames, afterSaleIds);
        }

        if (CollectionUtils.isNotEmpty(parentOrderIds)) {
            return getTableNames(tableNames, parentOrderIds);
        }
        return null;
    }

    /**
     * 获取真实的表名
     * @param columnValues 分片键的值
     * @return 匹配的表名集合
     */
    private Set<String> getTableNames(Collection<String> tableNames, Collection<String> columnValues) {
        Set<String> set = new HashSet<>();
        for(String columnValue : columnValues) {
            // 获取用户ID后三位
            String valueSuffix = (columnValue.length() < 3) ? columnValue : columnValue.substring(columnValue.length() - 3);
            String databaseSuffix = ShardingAlgorithmHelper.getTableSuffix(Integer.parseInt(valueSuffix));
            for(String tableName : tableNames) {
                if(tableName.endsWith(databaseSuffix)) {
                    set.add(tableName);
                }
            }
        }
        return set;
    }
}