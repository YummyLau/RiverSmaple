package com.effective.android.river;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    private static boolean sDebug = true;

    public static boolean isDebug() {
        return sDebug;
    }

    private static final RiverThreadPool sPool = new RiverThreadPool();

    public static RiverThreadPool getThreadPool() {
        return sPool;
    }

    private static volatile Set<String> sWaitForApplication = new HashSet<>();

    private static volatile List<Task> sRunBlockApplication = new ArrayList<>();

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static Handler getHandler() {
        return sHandler;
    }

    /**
     * 按照 priority 值来排序
     * 值越高，越优先
     */
    private final static Comparator<Task> sTaskComparator = new Comparator<Task>() {
        @Override
        public int compare(Task lhs, Task rhs) {
            return rhs.getPriority() - lhs.getPriority();
        }
    };

    public static Comparator<Task> getTaskComparator() {
        return sTaskComparator;
    }

    public static void addWaitTask(String id) {
        if (!TextUtils.isEmpty(id)) {
            sWaitForApplication.add(id);
        }
    }

    public static void addWaitTasks(String... ids) {
        if (ids != null && ids.length > 0) {
            for (String id : ids) {
                sWaitForApplication.add(id);
            }
        }
    }

    public static void removeWaitTask(String id) {
        if (!TextUtils.isEmpty(id)) {
            sWaitForApplication.remove(id);
        }
    }

    public static boolean hasWaitTasks() {
        return !sWaitForApplication.isEmpty();
    }

    public static void addRunTasks(Task task) {
        if (task != null && !sRunBlockApplication.contains(task)) {
            sRunBlockApplication.add(task);
        }
    }

    public static void tryRunBlockRunnable() {
        if (!sRunBlockApplication.isEmpty()) {
            if (sRunBlockApplication.size() > 1) {
                Collections.sort(sRunBlockApplication, Config.getTaskComparator());
            }
            Runnable runnable = sRunBlockApplication.remove(0);
            if (hasWaitTasks()) {
                runnable.run();
            } else {
                sHandler.post(runnable);
                for (Runnable blockItem : sRunBlockApplication) {
                    sHandler.post(blockItem);
                }
                sRunBlockApplication.clear();
            }
        }
    }

    public static boolean hasRunTasks() {
        return !sRunBlockApplication.isEmpty();
    }
}
