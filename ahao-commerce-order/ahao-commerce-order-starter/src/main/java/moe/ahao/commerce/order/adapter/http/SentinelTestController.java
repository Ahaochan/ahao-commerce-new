package moe.ahao.commerce.order.adapter.http;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.domain.entity.Result;
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
     */
    @GetMapping("/limitByQps")
    public Result<String> limitByQps(@RequestParam(required = false) String test) {
        return Result.success("limitByQps:" + test);
    }

    /**
     * 基于线程数限流
     */
    @GetMapping("/limitByThreadCount")
    public Result<String> limitByThreadCount(@RequestParam(required = false) String test) {
        try {
            // 模拟慢调用
            Thread.sleep(3000);
        } catch (Exception e) {
            log.error("error", e);
        }
        return Result.success("limitByThreadCount:" + test);
    }

    /**
     * 基于慢调用统计触发熔断降级
     */
    @GetMapping("/fallbackBySlowInvoke")
    public Result<String> fallbackBySlowInvoke(@RequestParam(required = false) String test) {
        try {
            // 模拟慢调用
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error("error", e);
        }
        return Result.success("fallbackBySlowInvoke:" + test);
    }

    private int num = 0;

    /**
     * 基于异常比例统计触发熔断降级
     */
    @GetMapping("/fallbackByErrorRatio")
    public Result<String> fallbackByErrorRatio(@RequestParam(required = false) String test) {
        num++;
        if (num % 2 == 0) {
            throw new RuntimeException("系统错误");
        }
        return Result.success("fallbackByErrorRatio:" + test);
    }


    /**
     * 基于异常数统计触发熔断降级
     */
    @GetMapping("/fallbackByErrorCount")
    public Result<String> fallbackByErrorCount(@RequestParam(required = false) String test) {
        num++;
        if (num % 2 == 0) {
            throw new RuntimeException("系统错误");
        }
        return Result.success("fallbackByErrorCount:" + test);
    }

    /**
     * API编程方式自定义sentinel资源
     */
    @GetMapping("/customResource_api")
    public Result<String> customResource_api(@RequestParam(required = false) String test) throws BlockException {
        try (Entry ignored = SphU.entry("SentinelTestController:customResource_api")) {
            return Result.success("customResource_api:" + test);
        } catch (BlockException e) {
            log.error("触发BlockException限流异常了", e);
            throw e;
        }
    }

    /**
     * 注解方式自定义sentinel资源
     */
    @SentinelResource(value = "SentinelTestController:customResource_annotation", blockHandler = "customResource_annotation_blockHandler", fallback = "customResource_annotation_fallback")
    @GetMapping("/customResource_annotation")
    public Result<String> customResource_annotation(@RequestParam(required = false) String test) {
        return Result.success("customResource_annotation:" + test);
    }

    public Result<String> customResource_annotation_blockHandler(String test, BlockException blockException) {
        log.error("触发BlockException限流异常了", blockException);
        return Result.success("customResource_annotation_blockHandler:" + test);
    }

    public Result<String> customResource_annotation_fallback(String test, Throwable e) {
        log.error("触发降级处理了", e);
        return Result.success("customResource_annotation_fallback:" + test);
    }

    /**
     * 通过userId定义黑名单的授权规则
     */
    @SentinelResource(value = "SentinelTestController:authRuleByUserId")
    @GetMapping("/authRuleByUserId")
    public Result<String> authRuleByUserId(@RequestParam(required = false) String test, HttpServletRequest request) {
        String userId = request.getHeader("user_id");
        return Result.success("userId=" + userId + ", test=" + test);
    }

}
