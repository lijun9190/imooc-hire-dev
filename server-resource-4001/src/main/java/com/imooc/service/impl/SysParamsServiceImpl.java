package com.imooc.service.impl;

import com.imooc.api.mq.DelayConfig_Industry;
import com.imooc.api.mq.DelayConfig_MaxCounts;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.DelayTimes;
import com.imooc.mapper.SysParamsMapper;
import com.imooc.pojo.SysParams;
import com.imooc.service.SysParamsService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class SysParamsServiceImpl extends BaseInfoProperties implements SysParamsService {

    @Autowired
    private SysParamsMapper sysParamsMapper;

    @Resource(name = "curatorClient")
    private CuratorFramework zkClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public Integer updateMaxResumeRefreshCounts(Integer maxCounts,
                                                Integer version) throws Exception {

        // 1. 把数据写入到数据库
        SysParams params = new SysParams();
        params.setId(SYS_PARAMS_PK);
        params.setMaxResumeRefreshCounts(maxCounts);

        sysParamsMapper.updateById(params);

        // 2. 把数据携带版本号保存到zk节点
        String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;

        Stat stat = zkClient.setData()
                .withVersion(version)
                .forPath(path,
                        maxCounts.toString().getBytes());

        // 3. 更新到缓存redis中
        redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, maxCounts + "");


        // 发送延迟队列来校验数据的一致性，db、redis、zk 这三者的数据需要达到一致性
        //int delayTimes = 20 * 1000;
        /*int delayTimes = DelayTimes.getDelayTimes(1);

        MessagePostProcessor processor = DelayConfig_Industry.setDelayTimes(delayTimes);
        rabbitTemplate.convertAndSend(
                DelayConfig_MaxCounts.EXCHANGE_DELAY_MAX_COUNTS,
                DelayConfig_MaxCounts.DELAY_MAX_COUNTS_REFRESH,
                "123456",
                processor);*/

        return stat.getVersion();
    }

    @Override
    public SysParams getSysParams() {
        return sysParamsMapper.selectById(SYS_PARAMS_PK);
    }
}
