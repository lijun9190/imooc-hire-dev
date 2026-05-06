package com.imooc.test.threads;

public class ThreadClass_01 extends Thread {

    @Override
    public void run() {
        System.out.println("我运行了。。。ThreadClass_01的编号为："
                + Thread.currentThread().getId());
    }

}
