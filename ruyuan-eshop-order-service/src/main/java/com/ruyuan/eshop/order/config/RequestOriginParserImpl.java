package com.ruyuan.eshop.order.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class RequestOriginParserImpl implements RequestOriginParser {

    @Override
    public String parseOrigin(HttpServletRequest request) {
        String userId = request.getHeader("user_id");
        log.warn("RequestOriginParserImpl:user_id:" + userId);
        if (StringUtils.isEmpty(userId)) {
            log.warn("user_id is not empty");
        }
        return userId;
    }
}