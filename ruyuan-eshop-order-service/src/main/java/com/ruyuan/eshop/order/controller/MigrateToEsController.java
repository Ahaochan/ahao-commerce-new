package com.ruyuan.eshop.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import com.ruyuan.eshop.order.elasticsearch.migrate.AfterSaleMigrateToEsHandler;
import com.ruyuan.eshop.order.elasticsearch.migrate.OrderMigrateToEsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 将现有数据迁移至es
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/es/migrate")
public class MigrateToEsController {

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private OrderMigrateToEsHandler orderMigrateToEsHandler;

    @Autowired
    private AfterSaleMigrateToEsHandler afterSaleMigrateToEsHandler;

    /**
     * 删除所有index
     */
    @GetMapping("/deleteAllIndex")
    public JsonResult<Boolean> deleteAllIndex() throws Exception {
        for (EsIndexNameEnum esIndexName : EsIndexNameEnum.values()) {
            esClientService.deleteIndex(esIndexName);
            System.out.println("删除index=" + esIndexName.getName());
        }
        return JsonResult.buildSuccess(true);
    }

    /**
     * 新建所有index
     */
    @GetMapping("/createAllIndex")
    public JsonResult<Boolean> createAllIndex() throws Exception {
        for (EsIndexNameEnum esIndexName : EsIndexNameEnum.values()) {
            esClientService.createIndexIfNotExists(esIndexName);
            System.out.println("新建index=" + esIndexName.getName());
        }
        return JsonResult.buildSuccess(true);
    }

    /**
     * 迁移订单
     */
    @PostMapping("/order")
    public JsonResult<Boolean> migrateOrder(@RequestBody OrderMigrateToEsHandler.Content content) throws Exception {
        orderMigrateToEsHandler.execute(JSONObject.toJSONString(content));
        return JsonResult.buildSuccess(true);
    }

    /**
     * 迁移售后单
     */
    @PostMapping("/afterSale")
    public JsonResult<Boolean> migrateAfterSale(@RequestBody AfterSaleMigrateToEsHandler.Content content) throws Exception {
        afterSaleMigrateToEsHandler.execute(JSONObject.toJSONString(content));
        return JsonResult.buildSuccess(true);
    }

}
