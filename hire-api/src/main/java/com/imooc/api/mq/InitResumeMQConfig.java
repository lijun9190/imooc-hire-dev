package com.imooc.api.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 的配置类
 */
@Configuration
public class InitResumeMQConfig {

    // 定义交换机的名称
    public static final String INIT_RESUME_EXCHANGE = "init_resume_exchange";

    // 定义队列的名称
    public static final String INIT_RESUME_QUEUE = "init_resume_queue";

    // 统一定义路由key
    public static final String ROUTING_KEY_INIT_RESUME = "init.resume.display";

    // 创建交换机
    @Bean(INIT_RESUME_EXCHANGE)
    public Exchange exchange() {
        return ExchangeBuilder
                    .topicExchange(INIT_RESUME_EXCHANGE)
                    .durable(true)
                    .build();
    }

    // 创建队列
    @Bean(INIT_RESUME_QUEUE)
    public Queue queue() {
        return QueueBuilder
                .durable(INIT_RESUME_QUEUE)
                .build();
    }

    // 创建绑定关系
    @Bean
    public Binding initResumeBinding(@Qualifier(INIT_RESUME_EXCHANGE) Exchange exchange,
                              @Qualifier(INIT_RESUME_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("init.resume.#")
                .noargs();
    }

}
