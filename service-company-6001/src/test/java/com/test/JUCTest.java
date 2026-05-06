package com.test;

import java.util.concurrent.Semaphore;

public class JUCTest {

    public static void main(String[] args) {

        // 定义信号量，声明资源的数量（停车场的车位数，餐厅的饭桌数）
        Semaphore semaphore = new Semaphore(3);

        for (int i = 0 ; i < 8 ; i ++) {
            new Thread(() -> {
                try {
                    semaphore.acquire();    // 获得资源数  --1
                    System.out.println(Thread.currentThread().getName() + "来吃饭了~");

                    Thread.sleep(3500);

                    semaphore.release();    // 释放资源    ++1
                    System.out.println(Thread.currentThread().getName() + "吃完走人~");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, i+1+"号顾客").start();
        }
    }

}
