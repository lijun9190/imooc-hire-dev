package com.imooc.api.mq;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 的配置类
 */
@Configuration
public class DelayConfig_MaxCounts {

    // 定义交换机的名称
    public static final String EXCHANGE_DELAY_MAX_COUNTS = "exchange_delay_max_counts";

    // 定义队列的名称
    public static final String QUEUE_DELAY_MAX_COUNTS = "queue_delay_max_counts";

    // 统一定义路由key
    public static final String DELAY_MAX_COUNTS_REFRESH = "delay.max.counts.refresh";

    // 创建交换机
    @Bean(EXCHANGE_DELAY_MAX_COUNTS)
    public Exchange exchange() {
        return ExchangeBuilder
                    .topicExchange(EXCHANGE_DELAY_MAX_COUNTS)
                    .durable(true)
                    .delayed()              // 设置延迟特性
                    .build();
    }

    // 创建队列
    @Bean(QUEUE_DELAY_MAX_COUNTS)
    public Queue queue() {
//        return new Queue(SMS_QUEUE);
        return QueueBuilder
                .durable(QUEUE_DELAY_MAX_COUNTS)
                .build();
    }

    // 创建绑定关系
    @Bean
    public Binding delayBindingMaxCounts(@Qualifier(EXCHANGE_DELAY_MAX_COUNTS) Exchange exchange,
                                         @Qualifier(QUEUE_DELAY_MAX_COUNTS) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(DELAY_MAX_COUNTS_REFRESH)
                .noargs();
    }

    /**
     * 设置消息属性处理器，目的是设置延迟的时间
     * @param times
     * @return
     */
    public static MessagePostProcessor setDelayTimes(Integer times) {
        return new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                // 设置持久属性
                message.getMessageProperties()
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                // 设置延迟的时间，单位毫秒
                message.getMessageProperties().setDelay(times);
                return message;
            }
        };
    }

}
