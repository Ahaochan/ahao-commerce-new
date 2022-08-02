package com.ruyuan.eshop.order.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.Map;

/**
 * HBase的配置
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(HBaseProperty.class)
public class HBaseConfig {

    /**
     * hbase相关配置
     */
    @Autowired
    private HBaseProperty hbaseProperty;

    /**
     * 配置hbase
     *
     * @return hbase的配置
     */
    private Configuration configuration() {
        Configuration configuration = HBaseConfiguration.create();
        Map<String, String> config = hbaseProperty.getConfig();
        config.forEach(configuration::set);
        return configuration;
    }

    /**
     * 获取hbase的连接
     *
     * @return hbase连接
     * @throws IOException 异常
     */
    @Bean
    public Connection getConnection() throws IOException {
        return ConnectionFactory.createConnection(configuration());
    }

}