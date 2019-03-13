package com.effective.android.riversample;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.effective.android.river.debug.Logger;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainApplication extends Application {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30;


    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "River Thread #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new PriorityBlockingQueue<>(128);


    @Override
    public void onCreate() {
        super.onCreate();
//        Logger.d("详情信息 MainApplication  start");
//        TaskTest taskTest = new TaskTest();
//        taskTest.start();
//        Logger.d("详情信息 MainApplication  end");

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue, sThreadFactory);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        for(int i = 100; i >= 0; i--){
            threadPoolExecutor.execute(new Task("task_" + i,i / 10));
        }
    }

    public static class Task implements Runnable, Comparable<Task> {

        public String name;
        public int pro;

        public Task(String name, int pro) {
            this.name = name;
            this.pro = pro;
        }

        @Override
        public int compareTo(@NonNull Task o) {
            if (pro < o.pro) {
                return 1;
            }
            if (pro > o.pro) {
                return -1;
            }
            return 0;
        }

        @Override
        public void run() {

            Log.d("MainApplication",    Thread.currentThread().getName() + " " + name + "(" + pro + ")" + " running!");
        }
    }
}
