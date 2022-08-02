package com.ruyuan.eshop.order.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * :elasticsearch client 配置bean
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Configuration
public class EsClientConfig {

    /**
     * 日志组件
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EsClientConfig.class);

    /**
     * 建立连接超时时间
     */
    public static int CONNECT_TIMEOUT_MILLIS = 1000;
    /**
     * 数据传输过程中的超时时间
     */
    public static int SOCKET_TIMEOUT_MILLIS = 30000;
    /**
     * 从连接池获取连接的超时时间
     */
    public static int CONNECTION_REQUEST_TIMEOUT_MILLIS = 500;

    /**
     * 路由节点的最大连接数
     */
    public static int MAX_CONN_PER_ROUTE = 10;
    /**
     * client最大连接数量
     */
    public static int MAX_CONN_TOTAL = 30;
    /**
     * 逗号分隔符
     */
    public static final String COMMA = ",";
    /**
     * 冒号分隔符
     */
    public static final String COLON = ":";

    /**
     * es集群节点，逗号分割
     */
    @Value("${elasticsearch.clusterNodes}")
    private String clusterNodes;

    /**
     * es rest client的bean对象
     */
    private RestHighLevelClient restHighLevelClient;

    /**
     * 加载es集群节点，逗号分隔
     *
     * @return 集群
     */
    private HttpHost[] loadHttpHosts() {
        String[] clusterNodesArray = clusterNodes.split(COMMA);
        HttpHost[] httpHosts = new HttpHost[clusterNodesArray.length];
        for (int i = 0; i < clusterNodesArray.length; i++) {
            String clusterNode = clusterNodesArray[i];
            String[] hostAndPort = clusterNode.split(COLON);
            httpHosts[i] = new HttpHost(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
        }
        return httpHosts;
    }

    /**
     * es client bean
     *
     * @return restHighLevelClient es高级客户端
     */
    @Bean
    public RestHighLevelClient restClient() {
        // 创建restClient的构造器
        RestClientBuilder restClientBuilder = RestClient.builder(loadHttpHosts());
        // 设置连接超时时间等参数
        setConnectTimeOutConfig(restClientBuilder);
        setConnectConfig(restClientBuilder);
        restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        return restHighLevelClient;
    }

    /**
     * 配置连接超时时间等参数
     *
     * @param restClientBuilder 创建restClient的构造器
     */
    private void setConnectTimeOutConfig(RestClientBuilder restClientBuilder) {
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            requestConfigBuilder.setSocketTimeout(SOCKET_TIMEOUT_MILLIS);
            requestConfigBuilder.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS);
            return requestConfigBuilder;
        });
    }

    /**
     * 使用异步httpclient时设置并发连接数
     *
     * @param restClientBuilder 创建restClient的构造器
     */
    private void setConnectConfig(RestClientBuilder restClientBuilder) {
        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(MAX_CONN_TOTAL);
            httpClientBuilder.setMaxConnPerRoute(MAX_CONN_PER_ROUTE);
            return httpClientBuilder;
        });
    }

    @PreDestroy
    public void close() {
        if (restHighLevelClient != null) {
            try {
                LOGGER.info("Closing the ES REST client");
                restHighLevelClient.close();
            } catch (IOException e) {
                LOGGER.error("Problem occurred when closing the ES REST client", e);
            }
        }
    }

}