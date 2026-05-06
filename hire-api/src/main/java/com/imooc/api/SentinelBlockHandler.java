package com.imooc.api;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class SentinelBlockHandler implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       BlockException e) throws Exception {
        GraceException.display(ResponseStatusEnum.SENTINEL_BLOCK_FLOW_LIMIT_ERROR);
    }
}
