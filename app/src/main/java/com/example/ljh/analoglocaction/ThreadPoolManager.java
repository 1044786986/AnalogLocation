package com.example.ljh.analoglocaction;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ljh on 2017/12/26.
 */

public class ThreadPoolManager {
    public static ThreadPoolManager threadPoolManager = new ThreadPoolManager();
    private int corePoolSize;
    private int maxPoolSize;
    private long keepTime = 1;
    private TimeUnit timeUnit = TimeUnit.HOURS;
    private ThreadPoolExecutor mThreadPoolExecutor;

    ThreadPoolManager(){
        corePoolSize = Runtime.getRuntime().availableProcessors()*2+1;
        maxPoolSize = corePoolSize;
        mThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize,maxPoolSize,keepTime,timeUnit,
                new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolManager getThreadPoolManager(){
        return threadPoolManager;
    }

    public void execute(Runnable runnable){
        if(runnable == null){
            return;
        }
        mThreadPoolExecutor.execute(runnable);
    }

    public void remove(Runnable runnable){
        if(runnable == null){
            return;
        }
        mThreadPoolExecutor.remove(runnable);
    }
}
