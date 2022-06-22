package com.cc.fileManage.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 */
public class CThreadPool {

    ///线程服务
    private final ExecutorService executor;

    private CThreadPool() {
        executor = new ThreadPoolExecutor(2, 6,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10));
    }

    private static class Inner{
        private static final CThreadPool SINGLE_TON = new CThreadPool();
    }

    public static CThreadPool getInstance(){
        return Inner.SINGLE_TON;
    }

    public static ExecutorService getExecutor(){
        return Inner.SINGLE_TON.executor;
    }

    /**
     * 执行线程
     * @param task  线程
     */
    public void executeAsynchronousTask(AsynchronousTask<?,?> task) {
        ///=======================
        if(task != null) {
            task.onPreExecute();
            executor.submit(task);
        }
    }

    /**
     * 取消线程
     * @param task  线程
     */
    public void stopAsynchronousTask(AsynchronousTask<?,?> task) {
        if(task != null) task.setCancel(true);
    }
}
