package com.ruyuan.eshop.order;

import com.ruyuan.consistency.annotation.EnableTendConsistencyTask;
import com.ruyuan.process.engine.annoations.EnableProcessEngine;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * swagger ui : http://localhost:8005/swagger-ui.html
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@EnableTendConsistencyTask
@EnableProcessEngine("ruyuan-order-process.xml")
@SpringBootApplication
@MapperScan("com.ruyuan.eshop.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}