package com.imooc.test.threads;

import java.util.UUID;
import java.util.concurrent.Callable;

public class CallableClass_02 implements Callable<Object> {

    @Override
    public Object call() throws Exception {

        System.out.println("我运行了。。。CallableClass_02的编号为："
                + Thread.currentThread().getId());

        return UUID.randomUUID().toString();
    }
}
