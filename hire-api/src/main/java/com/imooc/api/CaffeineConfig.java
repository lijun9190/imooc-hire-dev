package com.imooc.api;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.imooc.api.intercept.JWTCurrentUserInterceptor;
import com.imooc.api.intercept.SMSInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    /**
     * 声明统一缓存bean，所有数据都可以使用本cache
     * @return
     */
    @Bean
    public Cache<String, Object> cache() {
        return Caffeine.newBuilder()
                    .initialCapacity(50)    // 初始的缓存空间大小
                    .maximumSize(1000)
                    .build();
    }

    /**
     * 专门用于设置[简历刷新次数]的缓存
     * @return
     */
    @Bean
    public Cache<String, Integer> resumeRefreshCountsCache() {
        return Caffeine.newBuilder()
                    //.initialCapacity(1)
                    .maximumSize(1)
                    .build();
    }
}
