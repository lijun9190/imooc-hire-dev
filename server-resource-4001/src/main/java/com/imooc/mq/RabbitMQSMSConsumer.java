package com.imooc.mq;

import com.imooc.api.mq.RabbitMQSMSConfig;
import com.imooc.api.mq.TestMQSMSConfig;
import com.imooc.pojo.mq.SMSContentQO;
import com.imooc.utils.GsonUtils;
import com.imooc.utils.SMSUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 短信监听消费者
 */
@Slf4j
@Component
public class RabbitMQSMSConsumer {

    @Autowired
    private SMSUtils smsUtils;

    @RabbitListener(queues = {TestMQSMSConfig.TEST_QUEUE})
    public void watchQueue(String payload, Message message) throws Exception {

        log.info("payload = " + payload);

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("routingKey = " + routingKey);

        String msg = payload;
        log.info("msg = " + msg);

        if (routingKey.equalsIgnoreCase(TestMQSMSConfig.ROUTING_KEY_TEST)) {
            // 此处为短信发送的消息消费处理
            //SMSContentQO contentQO = GsonUtils.stringToBean(msg, SMSContentQO.class);
//            smsUtils.sendSMS(contentQO.getMobile(), contentQO.getContent());
        }
    }

    /**
     * 监听队列，并且处理消息
     * @param payload
     * @param message
     */
//    @RabbitListener(queues = {RabbitMQSMSConfig.SMS_QUEUE})
//    public void watchQueue(String payload, Message message) throws Exception {
//
//        log.info("payload = " + payload);
//
//        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
//        log.info("routingKey = " + routingKey);
//
//        String msg = payload;
//        log.info("msg = " + msg);
//
//        if (routingKey.equalsIgnoreCase(RabbitMQSMSConfig.ROUTING_KEY_SMS_SEND_LOGIN)) {
//            // 此处为短信发送的消息消费处理
//            SMSContentQO contentQO = GsonUtils.stringToBean(msg, SMSContentQO.class);
////            smsUtils.sendSMS(contentQO.getMobile(), contentQO.getContent());
//        }
//    }

    /**
     *
     * @param message
     * @param channel
     * @throws Exception
     */
//    @RabbitListener(queues = {RabbitMQSMSConfig.SMS_QUEUE})
    public void watchQueue(Message message, Channel channel) throws Exception {

        try {
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            log.info("routingKey = " + routingKey);

//            int a = 1/0;

            String msg = new String(message.getBody());
            log.info("msg = " + msg);

            /**
             * deliveryTag: 消息投递的标签
             * multiple: 批量确认所有消费者获得的消息
             */
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),
                            true);
        } catch (Exception e) {
            e.printStackTrace();
            /**
             * requeue: true：重回队列 false：丢弃消息
             */
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),
                    true,
                    false);
//            channel.basicReject();
        }

    }
}
