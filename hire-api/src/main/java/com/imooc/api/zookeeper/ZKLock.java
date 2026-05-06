package com.imooc.api.zookeeper;

import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZKLock {

    private ZooKeeper zooKeeper;                            // zookeeper客户端api
    private String lockName;                                // 创建锁的名称
    private static final String lockSpace = "/locks";       // 分布式锁的命名空间

    private CountDownLatch waitToGetLatch = new CountDownLatch(1);

    private String selfLock;

    public ZKLock(ZooKeeper zooKeeper, String lockName) {
        this.zooKeeper = zooKeeper;
        this.lockName = lockName;

        /**
         * 创建的同时初始化分布所的命名空间 /locks
         * 所有的分布式锁都在 【lockSpace】 之下
         */

        try {
            Stat stat = zooKeeper.exists(lockSpace, false);
            if (stat == null) {
                // 为空则创建节点；不为空则表示已经存在，则直接忽略
                zooKeeper.create(lockSpace,
                        "this is zk lock's namespace".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获得锁
     */
    public void get(){
        // 创建锁: 创建临时有序的节点 lockName-0000001
        try {
            selfLock = zooKeeper.create(lockSpace + "/" + lockName,
                                        null,
                                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                        CreateMode.EPHEMERAL_SEQUENTIAL);

            log.info("[{}]创建完毕，开始进入判断逻辑...", selfLock);

            // 判断当前节点，是不是处于第0个位置，如果是则表明当前请求可以获得分布式锁
            // 如果上一个节点还存在（当前不处于第0个位置），则需要等待上一个节点释放以后再获得锁

            // 获得lock命名空间下的所有子节点（分布式锁节点）
            List<String> subLocks = zooKeeper.getChildren(lockSpace, false);
            if (subLocks.isEmpty() || selfLock == null) {
                // 必定不为空（因为刚刚创建过selfLock），如果为空，灵异现象
                GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
            }

            if (subLocks.size() == 1) {
                // 如果子节点的数量只有一个，那么必定当前节点的顺序是最小有序值，则直接获得锁
                log.info("只有我一个人[{}]获得锁...", selfLock);
                return;
            } else {

                // 000001 <- 000002 <- 000003
                // 获得锁的下标进行判断

                // 对所有子节点进行排序
                Collections.sort(subLocks);

                // /locks/orders/imooc-lock00001  获得最后一个分隔符后面的字符串
                String shortSelfLock = StringUtils.substringAfterLast(selfLock, "/");
                // 获得当前节点所处位置的下标
                Integer selfIndex = Collections.binarySearch(subLocks, shortSelfLock);

                if (selfIndex == -1) {
                    // 如果没有截取到，说明数据可能出现问题
                    GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
                } else if (selfIndex > 0) {

                    // 下标大于0，说明当前节点不是第一个节点
                    String beforeLock = subLocks.get(selfIndex - 1);    // 获得上一个节点

                    // 监听上一个节点
                    zooKeeper.getData(lockSpace + "/" + beforeLock, event -> {

                        // 如果上一个节点被删除，则继续尝试获得锁
                        if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                            log.info("监听到[{}]的锁已被释放...", beforeLock);
                            waitToGetLatch.countDown();
                        }

                    }, null);


                    log.info("[{}]没有获得分布式锁，开始等待...", selfLock);
                    waitToGetLatch.await();

                    log.info("[{}]等待完毕，已获得锁...", selfLock);
                    return;

                } else if (selfIndex == 0){
                    // 下标等于0，说明当前节点就是第一个节点，允许获得锁
                    log.info("[{}]获得分布式锁，开始执行业务...", selfLock);
                    return;
                }

            }


        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    /**
     * 释放锁
     */
    public void release() {
        try {
            log.info("[{}]释放锁...", selfLock);
            zooKeeper.delete(selfLock, -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
