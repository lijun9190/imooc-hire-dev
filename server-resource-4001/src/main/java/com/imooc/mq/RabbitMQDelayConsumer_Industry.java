package com.imooc.mq;

import com.imooc.api.mq.DelayConfig_Industry;
import com.imooc.api.mq.RabbitMQSMSConfig_Dead;
import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.Industry;
import com.imooc.pojo.vo.TopIndustryWithThirdListVO;
import com.imooc.service.IndustryService;
import com.imooc.utils.GsonUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class RabbitMQDelayConsumer_Industry extends BaseInfoProperties {

    @Autowired
    private IndustryService industryService;

    @RabbitListener(queues = {DelayConfig_Industry.QUEUE_DELAY_REFRESH})
    public void watchQueue(Message message, Channel channel) throws Exception {

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("routingKey = " + routingKey);

        String msg = new String(message.getBody());
        log.info("msg = " + msg);
        log.info("当前时间为：" + LocalDateTime.now());

        if (routingKey.equalsIgnoreCase(DelayConfig_Industry.DELAY_REFRESH_INDUSTRY)) {
            log.info("10秒后监听到延迟队列");

            // 从Redis中删除一级分类
            redis.del(TOP_INDUSTRY_LIST);
            // 查询一级分类列表
            List<Industry> topIndustryList = industryService.getTopIndustryList();
            // 设置一级分类到Redis
            redis.set(TOP_INDUSTRY_LIST, GsonUtils.object2String(topIndustryList));


            // 从Redis中删除三级分类
            String thirdKeyMulti = THIRD_INDUSTRY_LIST + ":byTopId:";
            redis.allDel(thirdKeyMulti);
            // 查询三级分类列表
            List<TopIndustryWithThirdListVO> thirdIndustryList =
                    industryService.getAllThirdIndustryList();
            // 设置三级分类到Redis
            for (TopIndustryWithThirdListVO thirdVO : thirdIndustryList) {
                String topIndustryId = thirdVO.getTopId();
                String thirdKey = THIRD_INDUSTRY_LIST + ":byTopId:" + topIndustryId;
                redis.set(thirdKey, GsonUtils.object2String(thirdVO.getThirdIndustryList()));
            }

        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),
                        true);
    }
}
