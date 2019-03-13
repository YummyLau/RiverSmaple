package com.effective.android.river;


public class RiverManager {

    protected volatile static RiverManager sInstance = null;

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

    public RiverManager applicationBlockUntilTaskFinish(String taskId) {
        Config.addWaitTask(taskId);
        return this;
    }

    public RiverManager applicationBlockUntilTaskFinish(String... taskIds) {
        Config.addWaitTasks(taskIds);
        return this;
    }

    public synchronized void start(Task task) {
        task.start();
        while (Config.hasWaitTasks()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (Config.hasRunTasks()) {
                Config.tryRunBlockRunnable();
            }
        }
    }
}
