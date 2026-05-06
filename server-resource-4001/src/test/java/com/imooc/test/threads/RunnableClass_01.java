package com.imooc.test.threads;

public class RunnableClass_01 implements Runnable {

    @Override
    public void run() {
        System.out.println("我运行了。。。RunnableClass_01的编号为："
                + Thread.currentThread().getId());
    }

}
