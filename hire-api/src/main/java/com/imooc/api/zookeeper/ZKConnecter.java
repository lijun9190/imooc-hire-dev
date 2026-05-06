package com.imooc.api.zookeeper;

import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

//@Component
public class ZKConnecter {

    private static String host = "192.168.1.123:2181";
    private Integer timeout = 5 * 60 * 1000;        // 断点调试建议时间长一些

    private ZooKeeper zooKeeper;

    /**
     * 项目启动的时候初始化方法
     */
    @PostConstruct
    public void init() {
        // 获得zookeeper连接
        try {
            zooKeeper = new ZooKeeper(host, timeout, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 项目停止，SpringBoot容器停止之前，调用
     */
    @PreDestroy
    public void close() {
        // 关闭zookeeper连接
        try {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得锁
     * @param lockName
     */
    public ZKLock getLock(String lockName) {
        if (StringUtils.isBlank(lockName))
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR_NOT_BLANK);

        return new ZKLock(zooKeeper, lockName);
    }


}
