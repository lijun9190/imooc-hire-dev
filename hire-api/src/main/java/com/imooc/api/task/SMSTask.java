package com.imooc.api.task;

import com.imooc.api.retry.RetryComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SMSTask {

    @Autowired
    private RetryComponent retryComponent;

    @Async
    public void sendSMSTask() {
        boolean res = retryComponent.sendSmsWithRetry();
        log.info("异步任务 - 最终运行结果为 res = {}", res);
    }

}
