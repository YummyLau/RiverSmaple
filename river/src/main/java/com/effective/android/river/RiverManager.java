package com.effective.android.river;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RiverManager {

    protected volatile static RiverManager sInstance = null;
    protected volatile Set<Task> waitTasks = new HashSet<>();
    protected volatile List<Runnable> toRunTask = new ArrayList<>();

    private RiverManager() {
    }

    public static synchronized RiverManager getInstance() {
        if (sInstance == null) {
            synchronized (RiverManager.class) {
                if (sInstance == null) {
                    sInstance = new RiverManager();
                }
            }
        }
        return sInstance;
    }


    public synchronized void start(Task task, Task waitTask) {
        waitTasks.add(waitTask);
        task.start();
        while (!waitTasks.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!toRunTask.isEmpty()) {
                Runnable runnable = toRunTask.remove(0);
                runnable.run();
            }
        }
    }

}
