package com.imooc;

import com.alibaba.cloud.sentinel.gateway.scg.SentinelSCGAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        SentinelSCGAutoConfiguration.class
})
@EnableDiscoveryClient  // 开启注册中心的服务注册和发现功能
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
