package com.imooc.test.threads;

public class RunnableClass_02 implements Runnable {

    @Override
    public void run() {
        System.out.println("我运行了。。。RunnableClass_02的编号为："
                + Thread.currentThread().getId());
    }

}
