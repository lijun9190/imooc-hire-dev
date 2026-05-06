package com.imooc.test.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZKWatcherTest {

    public static ZooKeeper zooKeeper = null;

    CountDownLatch countDownLatch = new CountDownLatch(1);

    @Before
    public void initZK() throws Exception {
        zooKeeper = new ZooKeeper("192.168.1.123:2181",
                                            5 * 1000,
                                            null);
    }

    @Test
    public void getChildNodes() throws Exception {
        List<String> children = zooKeeper.getChildren("/imooc", event -> {
            Watcher.Event.EventType eventType = event.getType();
            String path = event.getPath();

            log.info("监听到路径为【{}】的事件类型为【{}】", path, eventType.toString());
        });

        countDownLatch.await();
    }

    @Test
    public void getNodeData() throws Exception {
        byte[] data = zooKeeper.getData("/imooc", event -> {
            Watcher.Event.EventType eventType = event.getType();
            String path = event.getPath();

            log.info("监听到路径为【{}】的事件类型为【{}】", path, eventType.toString());
        }, null);

        countDownLatch.await();
    }
}
