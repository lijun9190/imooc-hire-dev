package com.imooc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
@Slf4j
public class RabbitConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 回调函数
             * @param correlationData 相关性数据
             * @param ack 交换机是否成功接收到消息，true：成功
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData,
                                boolean ack,
                                String cause) {
                log.info("进入confirm");
                log.info("correlationData：{}", correlationData.getId());
                if (ack) {
                    log.info("交换机成功接收到消息~~ {}", cause);
                } else {
                    log.info("交换机接收消息失败~~失败原因： {}", cause);
                }
            }
        });
        return rabbitTemplate;
    }

}
