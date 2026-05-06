package com.imooc;

import com.imooc.base.BaseInfoProperties;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.SysParams;
import com.imooc.service.SysParamsService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 通过监听接口的方式，启动服务，服务初始化之后，执行方法
 */
@Configuration
public class SysConfig extends BaseInfoProperties implements CommandLineRunner {

    @Autowired
    private SysParamsService sysParamsService;

    @Resource(name = "curatorClient")
    private CuratorFramework zkClient;

    @Override
    public void run(String... args) throws Exception {
        SysParams sysParams =  sysParamsService.getSysParams();
        if (sysParams == null) GraceException.display(ResponseStatusEnum.SYS_DATA_ERROR);
        this.dealMaxResumeRefreshCounts(sysParams.getMaxResumeRefreshCounts());
    }

    private void dealMaxResumeRefreshCounts(int counts) {
        // 1. 把数据预热存储到redis中
        redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, counts+"");

        // 2. 把数据预热到zookeeper中
        String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;
        String data = counts+"";

        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                // 节点不存在，则创建
                zkClient.create()
                        .creatingParentContainersIfNeeded()     // /zk/imooc/abc/jack 递归创建节点路径
                        .withMode(CreateMode.PERSISTENT)        // 持久类型的节点
                        .forPath(path, data.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
