package com.effective.android.river;

import android.support.annotation.NonNull;

import com.effective.android.river.debug.DefaultTaskListener;
import com.effective.android.river.interfaces.TaskListener;
import com.effective.android.river.state.Stater;
import com.effective.android.river.state.TaskState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * created by yummylau on 2019/03/11
 */
public abstract class Task implements Runnable {

    @TaskState
    public int state = TaskState.IDLE;
    public String name;
    public boolean async;

    private List<Task> behindTasks = new ArrayList<>();                                //被依赖者
    private Set<Task> dependTasks = new HashSet<>();                                   //依赖者
    private Set<String> dependTaskName = new HashSet<>();                              //用于log统计
    public volatile boolean waitTasks = false;

    public static final int DEFAULT_EXECUTE_PRIORITY = 0;
    private int mExecutePriority = DEFAULT_EXECUTE_PRIORITY;
    private List<TaskListener> taskListeners = new ArrayList<>();

    public int getExecutePriority() {
        return mExecutePriority;
    }

    private static ExecutorService sExecutorService = Config.getExecutor();

    public Task(String name) {
        this(name, false);
    }

    public Task(String name, boolean async) {
        this.name = name;
        this.async = async;
        if (Config.isDebug()) {
            addTaskListener(new DefaultTaskListener());
        }
    }

    public void addTaskListener(TaskListener taskListener) {
        if (taskListener != null && !taskListeners.contains(taskListener)) {
            taskListeners.add(taskListener);
        }
    }

    @NonNull
    public List<TaskListener> getTaskListeners() {
        return taskListeners;
    }

    public synchronized void start() {
        if (!Stater.isIdelTask(this)) {
            throw new RuntimeException("You try to run task " + name + " twice, is there a circular dependency?");
        }
        Stater.toStart(this);
        if (async) {
            sExecutorService.execute(this);
        } else {
            this.run();
        }
    }

    @Override
    public void run() {
        Stater.toRuning(this);
        run(name);
        Stater.toFinish(this);
        notifyNextTask();
        waitUnitTaskFinish();
    }

    private void waitUnitTaskFinish() {
        while (waitTasks) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        recycle();
    }

    public abstract void run(String name);

    public Set<String> getDependTaskName() {
        return dependTaskName;
    }

    public void wait(Task task) {
        Stater.addWaitTask(this, task.name);
    }

    public void behind(Task task) {
        if (task != null && task != this) {
            task.dependOn(this);
            behindTasks.add(task);
        }
    }

    public void removeBehind(Task task) {
        if (task != null && task != this) {
            behindTasks.remove(task);
        }
    }

    public void dependOn(Task task) {
        if (task != null && task != this) {
            dependTasks.add(task);
            dependTaskName.add(task.name);
        }
    }

    public void removeDependence(Task task) {
        if (task != null && task != this) {
            dependTasks.remove(task);
        }
    }

    void notifyNextTask() {
        if (!behindTasks.isEmpty()) {

            if (behindTasks.size() > 1) {
                Collections.sort(behindTasks, new Comparator<Task>() {
                    @Override
                    public int compare(Task o1, Task o2) {
                        return o1.getExecutePriority() - o2.getExecutePriority();
                    }
                });
            }

            //遍历记下来的任务，通知它们说存在的前置已经完成
            for (Task task : behindTasks) {
                task.notifyBeforeTaskFinish(this);
            }
        }
    }

    void notifyBeforeTaskFinish(Task beforeTask) {

        if (dependTasks.isEmpty()) {
            return;
        }
        dependTasks.remove(beforeTask);

        //所有前置任务都已经完成了
        if (dependTasks.isEmpty()) {
            start();
        }
    }

    void recycle() {
        Stater.toRecycle(this);
        dependTasks.clear();
        behindTasks.clear();
        taskListeners.clear();
    }
}
