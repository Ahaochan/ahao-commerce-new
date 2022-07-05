package com.ruyuan.eshop.order.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.ruyuan.eshop.common.core.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * sentinel限流、熔断降级测试
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/sentinel/test")
public class SentinelTestController {

    /**
     * 基于qps限流
     *
     * @param test
     * @return
     */
    @GetMapping("/limitByQps")
    public JsonResult<String> limitByQps(@RequestParam(required = false) String test) {
        return JsonResult.buildSuccess("limitByQps:" + test);
    }

    /**
     * 基于线程数限流
     *
     * @param test
     * @return
     */
    @GetMapping("/limitByThreadCount")
    public JsonResult<String> limitByThreadCount(@RequestParam(required = false) String test) {
        try {
            // 模拟慢调用
            Thread.sleep(3000);
        } catch (Exception e) {
            log.error("error", e);
        }
        return JsonResult.buildSuccess("limitByThreadCount:" + test);
    }

    /**
     * 基于慢调用统计触发熔断降级
     *
     * @param test
     * @return
     */
    @GetMapping("/fallbackBySlowInvoke")
    public JsonResult<String> fallbackBySlowInvoke(@RequestParam(required = false) String test) {
        try {
            // 模拟慢调用
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error("error", e);
        }
        return JsonResult.buildSuccess("fallbackBySlowInvoke:" + test);
    }

    private int num = 0;

    /**
     * 基于异常比例统计触发熔断降级
     *
     * @param test
     * @return
     */
    @GetMapping("/fallbackByErrorRatio")
    public JsonResult<String> fallbackByErrorRatio(@RequestParam(required = false) String test) {
        num++;
        if(num % 2 == 0){
           throw new RuntimeException("系统错误");
        }
        return JsonResult.buildSuccess("fallbackByErrorRatio:" + test);
    }


    /**
     * 基于异常数统计触发熔断降级
     *
     * @param test
     * @return
     */
    @GetMapping("/fallbackByErrorCount")
    public JsonResult<String> fallbackByErrorCount(@RequestParam(required = false) String test) {
        num++;
        if(num % 2 == 0){
            throw  new RuntimeException("系统错误");
        }
        return JsonResult.buildSuccess("fallbackByErrorCount:" + test);
    }

    /**
     * API编程方式自定义sentinel资源
     *
     * @param test
     * @return
     */
    @GetMapping("/customResource_api")
    public JsonResult<String> customResource_api(@RequestParam(required = false) String test) throws BlockException {
        try (Entry ignored = SphU.entry("SentinelTestController:customResource_api")) {
            return JsonResult.buildSuccess("customResource_api:" + test);
        } catch (BlockException e) {
            log.error("触发BlockException限流异常了", e);
            throw e;
        }
    }

    /**
     * 注解方式自定义sentinel资源
     *
     * @param test
     * @return
     */
    @SentinelResource(value = "SentinelTestController:customResource_annotation", blockHandler = "customResource_annotation_blockHandler", fallback = "customResource_annotation_fallback")
    @GetMapping("/customResource_annotation")
    public JsonResult<String> customResource_annotation(@RequestParam(required = false) String test) {
        return JsonResult.buildSuccess("customResource_annotation:" + test);
    }

    public JsonResult<String> customResource_annotation_blockHandler(String test, BlockException blockException) {
        log.error("触发BlockException限流异常了", blockException);
        return JsonResult.buildSuccess("customResource_annotation_blockHandler:" + test);
    }

    public JsonResult<String> customResource_annotation_fallback(String test, Throwable e) {
        log.error("触发降级处理了", e);
        return JsonResult.buildSuccess("customResource_annotation_fallback:" + test);
    }

    /**
     * 通过userId定义黑名单的授权规则
     *
     * @param test
     * @return
     */
    @SentinelResource(value = "SentinelTestController:authRuleByUserId")
    @GetMapping("/authRuleByUserId")
    public JsonResult<String> authRuleByUserId(@RequestParam(required = false) String test, HttpServletRequest request)
            throws BlockException {
        String userId = request.getHeader("user_id");
        return JsonResult.buildSuccess("userId=" + userId + ", test=" + test);
    }

}