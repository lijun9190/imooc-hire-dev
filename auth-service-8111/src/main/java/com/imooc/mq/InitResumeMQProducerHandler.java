package com.imooc.mq;

import com.imooc.api.mq.InitResumeMQConfig;
import com.imooc.pojo.MqLocalMsgRecord;
import com.imooc.service.MqLocalMsgRecordService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MQ消息处理助手
 */
@Component
public class InitResumeMQProducerHandler {

    // 存储本地消息id列表
    private ThreadLocal<List<String>> msgIdsThreadLocal = new ThreadLocal<>();

    @Autowired
    private MqLocalMsgRecordService recordService;

    @Autowired
    public RabbitTemplate rabbitTemplate;

    /**
     * 保存消息到本地数据库
     * @param targetExchange
     * @param routingKey
     * @param msgContent
     */
    public void saveLocalMsg(String targetExchange, String routingKey, String msgContent) {

        MqLocalMsgRecord record = new MqLocalMsgRecord();
        record.setTargetExchange(targetExchange);
        record.setRoutingKey(routingKey);
        record.setMsgContent(msgContent);
        record.setCreatedTime(LocalDateTime.now());
        record.setUpdatedTime(LocalDateTime.now());

        recordService.save(record);

        // 从threadLocal中获得本地消息id，如果为空则初始化
        List<String> msgIds = msgIdsThreadLocal.get();
        if (CollectionUtils.isEmpty(msgIds)) {
            msgIds = new ArrayList<>();
        }
        // 每次消息存储在本地表中之后，都需要存储在当前线程中，事务提交以后可以对其进行检查
        msgIds.add(record.getId());
        msgIdsThreadLocal.set(msgIds);
    }

    /**
     * 发送本地所有的未处理消息给MQ
     */
    public void sendAllLocalMsg() {
        List<String> msgIds = msgIdsThreadLocal.get();
        if (CollectionUtils.isEmpty(msgIds)) {
            // 如果为空，那么则可能并不是当前业务所需要进行的消息发送，所以事务提交之后就可以直接完结了
            return;
        }

        // 根据msgIds获得当前数据库本地消息记录
        List<MqLocalMsgRecord> msgRecords = recordService.getBatchLocalMsgRecordList(msgIds);
        // 循环发送消息记录
        for (MqLocalMsgRecord record : msgRecords) {
            rabbitTemplate.convertAndSend(
                    record.getTargetExchange(),
                    record.getRoutingKey(),
                    record.getMsgContent() + "," + record.getId());
        }
    }

}
