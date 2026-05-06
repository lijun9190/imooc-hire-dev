package com.imooc;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class OSSConfig {

    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.fileHost}")
    private String fileHost;
    @Value("${oss.bucketName}")
    private String bucketName;
    @Value("${oss.accessKeyId}")
    private String accessKeyId;
    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    @Bean
    public OSSUtils creatOSSClient() {
        return new OSSUtils(endpoint, fileHost, bucketName, accessKeyId, accessKeySecret);
    }
}
