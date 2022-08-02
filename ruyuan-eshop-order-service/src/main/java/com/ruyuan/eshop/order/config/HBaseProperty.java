package com.ruyuan.eshop.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 从配置文件中读取HBase配置信息
 * 配置文件格式:hbase.config.*=xxx
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component("hbaseProperty")
@ConfigurationProperties(prefix = "hbase")
public class HBaseProperty {

    private Map<String, String> config;

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

}