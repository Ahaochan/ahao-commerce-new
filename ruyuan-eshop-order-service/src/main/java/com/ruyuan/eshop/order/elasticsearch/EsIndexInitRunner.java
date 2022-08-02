package com.ruyuan.eshop.order.elasticsearch;

import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * es index 初始化 runner
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
@Slf4j
public class EsIndexInitRunner implements ApplicationRunner {

    @Autowired
    private EsClientService esClientService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        for (EsIndexNameEnum element : EsIndexNameEnum.values()) {
            String indexName = element.getName();
            esClientService.createIndexIfNotExists(element);
            log.info("index={}已经创建", indexName);
        }
    }
}
