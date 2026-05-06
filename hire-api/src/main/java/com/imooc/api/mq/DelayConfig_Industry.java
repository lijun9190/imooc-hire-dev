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
public class DelayConfig_Industry {

    // 定义交换机的名称
    public static final String EXCHANGE_DELAY_REFRESH = "exchange_delay_refresh";

    // 定义队列的名称
    public static final String QUEUE_DELAY_REFRESH = "queue_delay_refresh";

    // 统一定义路由key
    public static final String DELAY_REFRESH_INDUSTRY = "delay.refresh.industry";

    // 创建交换机
    @Bean(EXCHANGE_DELAY_REFRESH)
    public Exchange exchange() {
        return ExchangeBuilder
                    .topicExchange(EXCHANGE_DELAY_REFRESH)
                    .durable(true)
                    .delayed()              // 设置延迟特性
                    .build();
    }

    // 创建队列
    @Bean(QUEUE_DELAY_REFRESH)
    public Queue queue() {
//        return new Queue(SMS_QUEUE);
        return QueueBuilder
                .durable(QUEUE_DELAY_REFRESH)
                .build();
    }

    // 创建绑定关系
    @Bean
    public Binding delayBindingIndustry(@Qualifier(EXCHANGE_DELAY_REFRESH) Exchange exchange,
                                        @Qualifier(QUEUE_DELAY_REFRESH) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("delay.refresh.*")
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
