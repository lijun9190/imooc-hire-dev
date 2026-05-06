package com.imooc.api.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class MyThreadPool {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3,
                10,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                Executors.defaultThreadFactory(),   // 默认的线程工厂
                new ThreadPoolExecutor.AbortPolicy()
        );
        return executor;
    }

}
