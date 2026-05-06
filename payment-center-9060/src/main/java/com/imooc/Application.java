package com.imooc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//@SpringBootApplication
@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@MapperScan(basePackages = "com.imooc.mapper")
@EnableDiscoveryClient                  // 开启服务注册与发现功能
//@EnableFeignClients("com.imooc.api.feign")  // 开启远程调用，并且制定扫描包
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @Bean
//    public Snowflake snowflake() {
//        return new Snowflake(new IdWorkerConfigBean());
//    }
}
