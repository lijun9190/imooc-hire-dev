package com.imooc.controller;

import com.imooc.api.mq.RabbitMQSMSConfig;
import com.imooc.api.mq.TestMQSMSConfig;
import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.mq.SMSContentQO;
import com.imooc.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("mq")
@Slf4j
public class TestMQController extends BaseInfoProperties {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("send")
    public GraceJSONResult send() {

//        RetryComponent.

        // 使用消息队列异步解耦发送短信
        //SMSContentQO contentQO = new SMSContentQO();
        //contentQO.setMobile("13961886188");
        //contentQO.setContent("abc123");

        // 定义confirm回调
        //rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
        //    /**
        //     * 回调函数
        //     * @param correlationData 相关性数据
        //     * @param ack 交换机是否成功接收到消息，true：成功
        //     * @param cause 失败的原因
        //     */
        //    @Override
        //    public void confirm(CorrelationData correlationData,
        //                        boolean ack,
        //                        String cause) {
        //        log.info("进入confirm");
        //        log.info("correlationData：{}", correlationData.getId());
        //        if (ack) {
        //            log.info("交换机成功接收到消息~~ {}", cause);
        //        } else {
        //            log.info("交换机接收消息失败~~失败原因： {}", cause);
        //        }
        //    }
        //});


        rabbitTemplate.convertAndSend(TestMQSMSConfig.TEST_EXCHANGE,
                TestMQSMSConfig.ROUTING_KEY_TEST,
                123,
                new CorrelationData(UUID.randomUUID().toString()));

        return GraceJSONResult.ok();
    }

}
