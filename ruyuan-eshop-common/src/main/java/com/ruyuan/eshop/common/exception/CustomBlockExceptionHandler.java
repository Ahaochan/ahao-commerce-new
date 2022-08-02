package com.ruyuan.eshop.common.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.utils.ServletUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义扩展sentinel的流控异常处理器
 *
 * @author zhonghuashishan
 * @version 1.0
 **/
@Component
public class CustomBlockExceptionHandler implements BlockExceptionHandler {


    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {

        JsonResult<Object> jsonResult = null;

        if(e instanceof FlowException){
            jsonResult = JsonResult.buildError("2002", "限流控制");
        }else if(e instanceof DegradeException){
            jsonResult = JsonResult.buildError("2003", "降级控制");
        } else if(e instanceof AuthorityException){
            jsonResult = JsonResult.buildError("2004", "授权控制");
        }
        httpServletResponse.setStatus(200);
        // 输出响应结果到前端
        ServletUtil.writeJsonMessage(httpServletResponse, jsonResult);
    }

}