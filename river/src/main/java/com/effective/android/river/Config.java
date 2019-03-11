package com.effective.android.river;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Config {

    private static boolean sDebug = true;

    private static int sCoreThreadNum = Runtime.getRuntime().availableProcessors();
    private static ThreadFactory sThreadFactory;
    private static ExecutorService sExecutor;


    /*package*/ public static boolean isDebug() {
        return sDebug;
    }

    /*package*/ static ThreadFactory getThreadFactory() {
        if (sThreadFactory == null) {
            sThreadFactory = getDefaultThreadFactory();
        }

        return sThreadFactory;
    }



    /*package*/ static ExecutorService getExecutor() {
        if (sExecutor == null) {
            sExecutor = getDefaultExecutor();
        }

        return sExecutor;
    }

    private static ThreadFactory getDefaultThreadFactory() {
        ThreadFactory defaultFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "River Thread #" + mCount.getAndIncrement());
            }
        };

        return defaultFactory;
    }

    private static ExecutorService getDefaultExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(sCoreThreadNum, sCoreThreadNum,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                getThreadFactory());
        executor.allowCoreThreadTimeOut(true);

        return executor;
    }
}
