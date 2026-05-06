package com.imooc.api.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 的配置类
 */
@Configuration
public class TestMQSMSConfig {

    // 定义交换机的名称
    public static final String TEST_EXCHANGE = "test_exchange";

    // 定义队列的名称
    public static final String TEST_QUEUE = "test_queue";

    // 统一定义路由key
    public static final String ROUTING_KEY_TEST = "imooc.send.test";

    // 创建交换机
    @Bean(TEST_EXCHANGE)
    public Exchange exchange() {
        return ExchangeBuilder
                    .topicExchange(TEST_EXCHANGE)
                    .durable(true)
                    .build();
    }

    // 创建队列
    @Bean(TEST_QUEUE)
    public Queue queue() {
        return new Queue(TEST_QUEUE);
    }

    // 创建绑定关系
    @Bean
    public Binding testMQBinding(@Qualifier(TEST_EXCHANGE) Exchange exchange,
                              @Qualifier(TEST_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("imooc.send.#")
                .noargs();
    }

}
