package com.imooc.curator;

import com.github.benmanes.caffeine.cache.Cache;
import com.imooc.base.BaseInfoProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "zookeeper.curator")
@Data
public class CuratorConfig extends BaseInfoProperties {

    private String host;                    // 单机/集群的ip:port地址
    private Integer connectionTimeoutMs;    // 连接超时时间
    private Integer sessionTimeoutMs;         // 会话超时时间
    private Integer sleepMsBetweenRetry;    // 每次重试的间隔时间
    private Integer maxRetries;             // 最大重试次数
    private String namespace;               // 命名空间（root根节点名称）

    public static final String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;

    @Autowired
    private Cache<String, Integer> cache;

    @Bean("curatorClient")
    public CuratorFramework curatorClient() {
        // 三秒后重连一次，只连一次
        //RetryPolicy retryOneTime = new RetryOneTime(3000);
        // 每3秒重连一次，重连3次
        //RetryPolicy retryNTimes = new RetryNTimes(3, 3000);
        // 每3秒重连一次，总等待时间超过10秒则停止重连
        //RetryPolicy retryPolicy = new RetryUntilElapsed(10 * 1000, 3000);
        // 随着重试次数的增加，重试的间隔时间也会增加（推荐）
        RetryPolicy backoffRetry = new ExponentialBackoffRetry(sleepMsBetweenRetry, maxRetries);

        // 声明初始化客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(host)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(backoffRetry)
                .namespace(namespace)
                .build();
        client.start();     // 启动curator客户端

        //try {
        //    client.create().forPath("/abc", "123".getBytes());
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        // 注册事件
        add(path, client);

        return client;
    }

    /**
     * 注册节点的事件监听
     * @param path
     * @param client
     */
    public void add(String path, CuratorFramework client) {

        CuratorCache curatorCache = CuratorCache.build(client, path);
        curatorCache.listenable().addListener((type, oldData, data) -> {
            // type: 当前监听到的事件类型
            // oldData: 节点更新前的数据、状态
            // data: 节点更新后的数据、状态

            //System.out.println(type.name());

            //NODE_CREATED
            //NODE_CHANGED
            //NODE_DELETED

            switch (type.name()) {
                case "NODE_CREATED":
                    log.info("(子)节点创建");
                    break;
                case "NODE_CHANGED":
                    log.info("(子)节点数据变更，oldData.getPath() = " + oldData.getPath());

                    if (oldData != null && oldData.getPath().equals(path)) {
                        log.info("监听到" + path + "的数据发生变更...");

                        Integer oldMaxCounts = (Integer)cache
                                                            .asMap()
                                                            .get(CACHE_MAX_RESUME_REFRESH_COUNTS);
                        log.info("原来的oldMaxCounts = " + oldMaxCounts);

                        Integer newMaxCounts = Integer.valueOf(new String(data.getData()));
                        // 各个微服务节点监听到变动，则更新各自的本地缓存
                        cache.put(CACHE_MAX_RESUME_REFRESH_COUNTS, newMaxCounts);

                        log.info("简历微服务节点本地缓存已更新...更新后的[最大刷新阈值]为：{}", newMaxCounts);
                    }

                    break;
                case "NODE_DELETED":
                    log.info("(子)节点删除");
                    break;
                default:
                    break;
            }

        });

        curatorCache.start();
    }

}
