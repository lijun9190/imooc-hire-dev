package com.imooc.test.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;

@Slf4j
public class ZKTest {

    @Test
    public void initZKTest() throws Exception {

        /**
         * 客户端和zk服务端链接的一个初始化方法(非阻塞的方式（异步方式）)
         * connectString: 链接服务器的ip字符串（可以是单个，或者用逗号间隔的集群ip地址）
         * sessionTimeout: 超时时间，心跳收不到，则超时
         * watcher: 监听通知的回调事件
         * sessionId: 会话id
         * sessionPasswd: 会话密码
         * canBeReadOnly: 可读
         */
        ZooKeeper zooKeeper = new ZooKeeper("192.168.1.123:2181",
                                            5 * 1000,
                                            null);

        log.info("客户端开始链接zookeeper服务器...");
        log.info("连接状态: {}", zooKeeper.getState());

        Thread.sleep(2000);

        log.info("连接状态: {}", zooKeeper.getState());
    }

}
