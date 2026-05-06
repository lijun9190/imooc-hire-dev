package com.imooc.mq;

import com.imooc.api.mq.DelayConfig_Industry;
import com.imooc.api.mq.DelayConfig_MaxCounts;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.DelayTimes;
import com.imooc.pojo.SysParams;
import com.imooc.service.SysParamsService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Component
public class RabbitMQDelayConsumer_MaxCounts extends BaseInfoProperties {

    @Autowired
    private SysParamsService sysParamsService;

    @Resource(name = "curatorClient")
    private CuratorFramework zkClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static final String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;

    @RabbitListener(queues = {DelayConfig_MaxCounts.QUEUE_DELAY_MAX_COUNTS})
    public void watchQueue(Message message, Channel channel) {

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String msg = new String(message.getBody());

        try {
            if (routingKey.equalsIgnoreCase(DelayConfig_MaxCounts.DELAY_MAX_COUNTS_REFRESH)) {

                /**
                 * 校验数据一致性，zk与redis都要和数据库中的数据保持一致
                 */

                // 1. 查询数据库
                SysParams sysParams = sysParamsService.getSysParams();
                Integer maxCounts = sysParams.getMaxResumeRefreshCounts();

                // 2. 查询zookeeper
                Integer maxCountsZK = Integer.valueOf(new String(zkClient.getData().forPath(path)));

                // 3. 查询Redis
                String maxCountsRedisStr = redis.get(REDIS_MAX_RESUME_REFRESH_COUNTS);
                Integer maxCountsRedis = 0;
                if (StringUtils.isBlank(maxCountsRedisStr)) {
                    redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, maxCounts + "");
                    maxCountsRedis = maxCounts;
                } else {
                    maxCountsRedis = Integer.valueOf(maxCountsRedisStr);
                }

                // 4. 如果和db数据库不一致，那么重新把db设置进zk以及redis中
                if (maxCountsZK != maxCounts || maxCountsRedis != maxCounts) {
                    resetMaxCounts(maxCounts);
                }

                // 模拟除零异常
                //int a = 1 / 0;

                redis.del(DELAY_ERROR_RETRY_COUNTS);
            }
        } catch (Exception e) {
            e.printStackTrace();

            // 如果发生异常，则再次发送延迟队列
            String delayErrorRetryCountsStr = redis.get(DELAY_ERROR_RETRY_COUNTS);
            Integer delayErrorRetryCounts = 1;
            if (StringUtils.isNotBlank(delayErrorRetryCountsStr)) {
                delayErrorRetryCounts = Integer.valueOf(delayErrorRetryCountsStr);
            }

            int delayTimes = DelayTimes.getDelayTimes(delayErrorRetryCounts);

            MessagePostProcessor processor = DelayConfig_Industry.setDelayTimes(delayTimes);
            rabbitTemplate.convertAndSend(
                    DelayConfig_MaxCounts.EXCHANGE_DELAY_MAX_COUNTS,
                    DelayConfig_MaxCounts.DELAY_MAX_COUNTS_REFRESH,
                    "123456",
                    processor);

            log.info("发生异常，这是第{}次发送延迟队列进行校验...", delayErrorRetryCounts);

            // 每次异常失败，则累加 DELAY_ERROR_RETRY_COUNTS
            redis.increment(DELAY_ERROR_RETRY_COUNTS, 1);
        }
    }

    private void resetMaxCounts(Integer maxCounts) throws Exception {
        // 覆盖redis
        redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, maxCounts + "");

        // 覆盖zookeeper
        zkClient.setData().forPath(path, maxCounts.toString().getBytes());

        log.info("zookeeper和redis已经被重置...");
    }
}
