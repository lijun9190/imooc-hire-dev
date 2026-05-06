package com.imooc.test.threads;

public class ThreadClass_02 extends Thread {

    @Override
    public void run() {
        System.out.println("我运行了。。。ThreadClass_02的编号为："
                + Thread.currentThread().getId());
    }
    
}
