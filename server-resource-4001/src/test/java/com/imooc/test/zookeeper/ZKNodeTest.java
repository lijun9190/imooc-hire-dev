package com.imooc.test.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@Slf4j
public class ZKNodeTest {

    public static ZooKeeper zooKeeper = null;

    @Before
    public void initZK() throws Exception {
        zooKeeper = new ZooKeeper("192.168.1.123:2181",
                                            5 * 1000,
                                            null);
    }

    @Test
    public void createNode() throws Exception {
        zooKeeper.create("/imooc/java",
                        "abc".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
    }

    @Test
    public void exitNode() throws Exception {
        Stat stat1 = zooKeeper.exists("/imooc/java", false);
        Stat stat2 = zooKeeper.exists("/imooc/js", false);

        if (stat1 == null) {
            log.info("/imooc/java 不存在");
        } else {
            log.info(stat1.toString());
        }

        if (stat2 == null) {
            log.info("/imooc/js 不存在");
        } else {
            log.info(stat2.toString());
        }
    }

    @Test
    public void getNodeData() throws Exception {
        byte[] data = zooKeeper.getData("/imooc/java", false, null);
        String content = new String(data);

        log.info(content);
    }

    @Test
    public void getChildNodes() throws Exception {
        List<String> children = zooKeeper.getChildren("/imooc", false);

        for (String c : children) {
            log.info(c);
        }
    }

    @Test
    public void updateNodeData() throws Exception {

        Stat javaStat = zooKeeper.exists("/imooc/java", false);

        // 版本号用于控制乐观锁，设置不同，则报错
        zooKeeper.setData("/imooc/java", "xyz".getBytes(), javaStat.getVersion());

        // 重新获得
        byte[] data = zooKeeper.getData("/imooc/java", false, null);
        String content = new String(data);

        log.info(content);
    }

    @Test
    public void deleteNode() throws Exception {

        Stat javaStat = zooKeeper.exists("/imooc/java", false);

        zooKeeper.delete("/imooc/java", javaStat.getVersion());

    }
}
