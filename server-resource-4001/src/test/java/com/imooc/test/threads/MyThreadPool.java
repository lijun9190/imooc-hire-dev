package com.imooc.test.threads;

import java.util.concurrent.*;

public class MyThreadPool {

    public static ExecutorService executorService
            = Executors.newFixedThreadPool(3);
    // Executors.newCachedThreadPool: 缓存线程池，线程会被回收
    // Executors.newFixedThreadPool: 固定数量线程池，线程不会被回收
    // Executors.newSingleThreadExecutor: 单线程线程池，单线程从队列中获得任务并且顺序执行
    // Executors.newScheduledThreadPool: 定时任务线程池


    /**
     * int corePoolSize: 核心线程数量，线程池一旦创建好，就有了固定的线程数，
     *                   这些线程(数)用于接收并且处理异步任务；
     *                   如果任务完结，那么这些线程依然还会存在，处于空闲状态，
     *                   除非设置allowCoreThreadTimeOut
     * int maximumPoolSize: 线程池中允许的最大线程数量，如果请求任务并发超过corePoolSize，
     *                      那么，最大的线程数也不会超过maximumPoolSize（弹性伸缩）
     * long keepAliveTime: 超时时间，当超过corePoolSize的部分线程数超时了，则释放
     * TimeUnit unit: 超时的时间单位
     * BlockingQueue<Runnable> workQueue: 阻塞队列，任务执行之前，
     *                                    超出的任务会先保存到一个队列中，
     *                                    线程一旦空闲，则从队列中获得新的任务开始执行
     * ThreadFactory threadFactory: 线程工厂，怎么创建线程的
     * RejectedExecutionHandler handler: 驳回策略，队列满了，或者队列达到性能的瓶颈，
     *                                   使用子的策略方式来处理任务的驳回
     *                   AbortPolicy - : 新任务进来，则直接丢弃（抛异常）
     *              CallerRunsPolicy - : 不用线程，直接调用run方法，为了保证任务一个都不丢失，
     *                                   但是会出现数据处理挤压的情况
     *           DiscardOldestPolicy - : 丢弃老的任务，使用新的任务
     *                 DiscardPolicy - : 丢弃新的任务（不会抛出异常）
     *
     */
    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            3,
            10,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(5000),
            Executors.defaultThreadFactory(),   // 默认的线程工厂
            new ThreadPoolExecutor.AbortPolicy()
    );


}
