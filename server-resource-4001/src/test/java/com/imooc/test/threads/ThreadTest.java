package com.imooc.test.threads;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class ThreadTest {

    /**
     * 实现多线程的启动方式
     * 1. 集成Thread
     * 2. 实现Runnable接口
     * 3. 实现callable接口
     * 4. 线程池
     */

    @Test
    public void ThreadClassTest() {
        System.out.println("开始运行test。。。");

        ThreadClass_01 threadClass01 = new ThreadClass_01();
        ThreadClass_02 threadClass02 = new ThreadClass_02();

        threadClass01.start();
        threadClass02.start();

        System.out.println("结束运行test。。。");
    }

    @Test
    public void RunnableClassTest() {
        System.out.println("开始运行test。。。");

        RunnableClass_01 runnableClass01 = new RunnableClass_01();
        RunnableClass_02 runnableClass02 = new RunnableClass_02();

        new Thread(runnableClass01).start();
        new Thread(runnableClass02).start();

        System.out.println("结束运行test。。。");
    }

    @Test
    public void CallableTest() throws Exception {
        System.out.println("开始运行test。。。");

        FutureTask<Object> futureTask01 = new FutureTask<>(new CallableClass_01());
        FutureTask<Object> futureTask02 = new FutureTask<>(new CallableClass_02());

        // 等待异步线程执行完毕之后，获得该值（同SpringBoot异步任务）
        new Thread(futureTask01).start();
        new Thread(futureTask02).start();

        Object o1 = futureTask01.get();
        Object o2 = futureTask02.get();

        System.out.println("o1 = " + o1);
        System.out.println("o2 = " + o2);

        // 最后打印并没有最先运行，因为上面的打印结果是同步的，是阻塞
        System.out.println("结束运行test。。。");
    }


    @Test
    public void ThreadPoolTest() throws Exception {

        // 固定的数量 fixed
//        ExecutorService executorService = Executors.newFixedThreadPool(5);

        Future<Object> futureTask = MyThreadPool.executorService
                                                .submit(new CallableClass_01());

        System.out.println("线程池执行结果。。。futureTask = " + futureTask.get());
    }
}
