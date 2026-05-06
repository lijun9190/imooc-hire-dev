package com.imooc.test.threads;

import com.imooc.utils.LocalDateUtils;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class CompletableFutureTest {


    @Test
    public void CompletableFutureTest() throws Exception {
        System.out.println("开始运行test。。。");

        /**
         * Thread+Runnable: 异步线程，但是无法获得返回值
         * Callable+Thread: 异步线程可以获得返回值，但是会阻塞当前的主线程
         */

//        CompletableFuture<Void> voidFuture = CompletableFuture.runAsync(
//                                                    new RunnableClass_01(),
//                                                    MyThreadPool.executorService);

        CompletableFuture<Void> voidFuture = CompletableFuture.runAsync(
                                            () -> {
                                                System.out.println("我运行了。。。RunnableClass_01的编号为："
                                                        + Thread.currentThread().getId());
                                            },
                                            MyThreadPool.executorService);

        CompletableFuture<String> stringFuture = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("内部打印。。。uuid = " + uuid);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return uuid;
        }, MyThreadPool.executorService);

        String outUUID = stringFuture.get();
        System.out.println("外部打印。。。uuid = " + outUUID);

        System.out.println("结束运行test。。。");
    }

    @Test
    public void CompletableFutureTestWhen() throws Exception {

        CompletableFuture<String> stringFuture = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("内部打印。。。uuid = " + uuid);
//            int a = 1 / 0;
            return uuid;
        }, MyThreadPool.executorService)
                .whenComplete((s, throwable) -> {
                    System.out.println("执行完毕后打印。。。CompleteUUID = " + s);
                    System.out.println(throwable.getMessage());
                })
                .exceptionally(throwable -> {
                    System.out.println(throwable.getMessage());

                    String uuid = UUID.randomUUID().toString();
                    System.out.println("exceptionally兜底打印。。。uuid = " + uuid);

                    return uuid;
                });

        /**
         * whenComplete: 业务执行完毕后，执行（同一个线程）
         * whenCompleteAsync: 业务执行完毕后，再开一个线程以异步的形式执行
         */
//        stringFuture.whenComplete((s, throwable) -> {
//            System.out.println("执行完毕后打印。。。CompleteUUID = " + s);
//            System.out.println(throwable.getMessage());
//        });

        /**
         * 发生异常之后，可以重新生成一个值，来替换原有的值，作为一个兜底
         * 比如：现在限流1万开始出现问题，那么则可以把限流值设为5000
         */
//        stringFuture.exceptionally(throwable -> {
//            System.out.println(throwable.getMessage());
//
//            String uuid = UUID.randomUUID().toString();
//            System.out.println("exceptionally兜底打印。。。uuid = " + uuid);
//
//            return uuid;
//        });

        String outUUID = stringFuture.get();
        System.out.println("外部打印。。。uuid = " + outUUID);
    }

    @Test
    public void CompletableFutureTestHandle() throws Exception {

        CompletableFuture<String> stringFuture = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("内部打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService)
            .handle((s, throwable) -> {
                return s + " ~~~~~ " + LocalDateUtils.getLocalTimeStr();
            });

        String outUUID = stringFuture.get();
        System.out.println("外部打印。。。uuid = " + outUUID);
    }

    @Test
    public void CompletableFutureTestThenRun() throws Exception {

        CompletableFuture<Void> stringFuture = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务1。。打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService)
            .thenRun(() -> {
                String uuid = UUID.randomUUID().toString();
                System.out.println("任务2。。打印。。。uuid = " + uuid);
            });
    }

    @Test
    public void CompletableFutureTestThenAccept() throws Exception {

        CompletableFuture<Void> stringFuture = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务1。。打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService)
                .thenAcceptAsync(s -> {
                    String uuid2 = UUID.randomUUID().toString();
                    System.out.println("上一个任务1的 UUID = " + s);
                    System.out.println("任务2。。打印。。。uuid2 = " + uuid2);
                }, MyThreadPool.executorService);
    }

    @Test
    public void CompletableFutureTestThenApply() throws Exception {

        CompletableFuture<String> stringFuture = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务1。。打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService)
                .thenApply(s -> {
                    String uuid2 = UUID.randomUUID().toString();
                    System.out.println("上一个任务1的 UUID = " + s);
                    System.out.println("任务2。。打印。。。uuid2 = " + uuid2);
                    return uuid2;
                })
                .thenApply(s -> {
                    String uuid3 = UUID.randomUUID().toString();
                    System.out.println("上一个任务2的 UUID = " + s);
                    System.out.println("任务3。。打印。。。uuid3 = " + uuid3);
                    return uuid3;
                })
                ;

        String outUUID = stringFuture.get();
        System.out.println("最终执行完毕的结果。。。outUUID = " + outUUID);
    }

    @Test
    public void CompletableFutureTestRunAfter() throws Exception {

        CompletableFuture<Void> completableFuture1 = CompletableFuture.runAsync(
                                                new RunnableClass_01(),
                                                MyThreadPool.executorService);

        CompletableFuture<Void> completableFuture2 = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("内部打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService)
//            .runAfterBoth(completableFuture1, () -> {
//                System.out.println("两个任务都完成啦~~~~");
//            })
            .runAfterEither(completableFuture1, () -> {
                System.out.println("其中一个任务完成啦~~~~");
            });
    }

    @Test
    public void CompletableFutureTestAccept() throws Exception {

        CompletableFuture<String> completableFuture1 = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务1打印。。。uuid = " + uuid);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return uuid;
        }, MyThreadPool.executorService);

        CompletableFuture<Void> completableFuture2 = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务2打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService)
//            .thenAcceptBoth(completableFuture1, (s, aVoid) -> {
//                System.out.println("thenAcceptBoth...uuid = " + s);
//            })
            .acceptEither(completableFuture1, s -> {
                System.out.println("acceptEither..." + s);
            })
        ;
    }

    @Test
    public void CompletableFutureTestApply() throws Exception {

        CompletableFuture<String> completableFuture1 = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务1打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService);

        CompletableFuture<String> completableFutureTotal = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务2打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService)
//            .thenCombine(completableFuture1, (s1, s2) -> {
//                System.out.println("s1 = " + s1);
//                System.out.println("s2 = " + s2);
//                return "两个参数都获得并且打印了";
//            })
            .applyToEither(completableFuture1, s -> {
                return s;
            })
                ;

        String res = completableFutureTotal.get();
        System.out.println("res = " + res);
    }


    @Test
    public void CompletableFutureTestAll() throws Exception {

        CompletableFuture<Void> completableFuture0 =
                CompletableFuture.runAsync(new RunnableClass_01(),
                                           MyThreadPool.executorService);

        CompletableFuture<String> completableFuture1 = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务1打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService);

        CompletableFuture<String> completableFuture2 = CompletableFuture.supplyAsync(() -> {
            String uuid = UUID.randomUUID().toString();
            System.out.println("任务2打印。。。uuid = " + uuid);
            return uuid;
        }, MyThreadPool.executorService);

//        CompletableFuture<Void> allOfFuture = CompletableFuture
//                                    .allOf(completableFuture0,
//                                           completableFuture1,
//                                           completableFuture2);
//        allOfFuture.get();
//        System.out.println("任务全部执行完毕~~");


        CompletableFuture<Object> anyOfFuture = CompletableFuture
                                    .anyOf(completableFuture0,
                                           completableFuture1,
                                           completableFuture2);
        anyOfFuture.get();

        System.out.println("任意一个任务执行完毕~~");
    }
}
