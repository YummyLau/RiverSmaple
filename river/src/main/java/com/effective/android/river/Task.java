package com.effective.android.river;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.SparseArray;

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
    private List<ITaskListener> taskListeners = new ArrayList<>();

    private static Handler sHandler = new Handler(Looper.getMainLooper());

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
        addTaskListener(new InnerTaskListener());
    }

    public void addTaskListener(ITaskListener taskListener) {
        if (taskListener != null && !taskListeners.contains(taskListener)) {
            taskListeners.add(taskListener);
        }
    }

    @NonNull
    public List<ITaskListener> getTaskListeners() {
        return taskListeners;
    }

    public synchronized void startWithDowmGraph() {

        StringBuilder stringBuilder = new StringBuilder("Graph start : ");

        stringBuilder.append(this.name);
        for (Task task : behindTasks) {

        }
        stringBuilder.append("Graph end  !");


        if (!TaskHelper.isTaskIdle(this)) {
            throw new RuntimeException("You try to run task " + name + " twice, is there a circular dependency?");
        }
        TaskHelper.toStart(this);
        if (async) {
            sExecutorService.execute(this);
        } else {
            sHandler.post(this);
        }
    }

//    public String getBehindInfo(int height) {
//        if (behindTasks.isEmpty()) {
//            return name;
//        }
//        for (Task task : behindTasks) {
//
//        }
//    }

    public synchronized void start() {
        if (!TaskHelper.isTaskIdle(this)) {
            throw new RuntimeException("You try to run task " + name + " twice, is there a circular dependency?");
        }
        TaskHelper.toStart(this);
        if (async) {
            sExecutorService.execute(this);
        } else {
            sHandler.post(this);
        }
    }

    @Override
    public void run() {
        TaskHelper.toRunning(this, Thread.currentThread().getName());
        run(name);
        TaskHelper.toFinish(this);
        notifyNextTask();
        waitUnitTaskFinish();
    }

    private void waitUnitTaskFinish() {
        TaskHelper.toBlock(this);
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
        TaskHelper.addWaitTask(this, task.name);
    }

    public void behind(Task task) {
        if (task != null && task != this) {
            task.dependTasks.add(this);
            task.dependTaskName.add(this.name);
            behindTasks.add(task);
        }
    }

    public void removeBehind(Task task) {
        if (task != null && task != this) {
            behindTasks.remove(task);
            task.dependTasks.remove(this);
        }
    }

    public void dependOn(Task task) {
        if (task != null && task != this) {
            dependTasks.add(task);
            dependTaskName.add(task.name);
            task.behindTasks.add(this);
        }
    }

    public void removeDependence(Task task) {
        if (task != null && task != this) {
            dependTasks.remove(task);
            task.behindTasks.remove(this);
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
        TaskHelper.toRecycle(this);
        dependTasks.clear();
        behindTasks.clear();
        taskListeners.clear();
    }


    public static class InnerTaskListener implements ITaskListener {

        @Override
        public void onStart(Task task) {
            Logger.d(task.name + " -- onStart -- ");
        }

        @Override
        public void onRunning(Task task) {
            Logger.d(task.name + " -- onRunning -- ");
        }

        @Override
        public void onFinish(Task task) {
            Logger.d(task.name + " -- onFinish -- ");
        }

        @Override
        public void onBlock(Task task) {
            Logger.d(task.name + " -- onBlock -- ");
        }

        @Override
        public void onRecycled(Task task) {
            Logger.d(task.name + " -- onRecycled -- ");
            Logger.d(getTaskRuntimeInfoString(task));
        }

        public static String getTaskRuntimeInfoString(Task task) {
            TaskRuntimeInfo taskRuntimeInfo = TaskInfoCollections.getTaskRuntimeInfo(task);
            if (taskRuntimeInfo == null) {
                return "";
            }
            SparseArray<Long> map = taskRuntimeInfo.stateTime;
            Long startTime = map.get(TaskState.START);
            Long runningTime = map.get(TaskState.RUNNING);
            Long finishedTime = map.get(TaskState.FINISHED);
            Long blockTime = map.get(TaskState.BLOCK);
            Long recycledTime = map.get(TaskState.RECYCLED);
            StringBuilder builder = new StringBuilder();
            builder.append("\n");
            builder.append("详情信息");
            builder.append("\n");
            builder.append("=======================" + (taskRuntimeInfo.isProject ? "project" : "task") + "( " + task.name + " )=============================");
            builder.append("\n");
            builder.append("| 任务依赖 : " + getDependenceInfo(taskRuntimeInfo));
            builder.append("\n");
            builder.append("| 线程信息 : " + taskRuntimeInfo.threadName);
            builder.append("\n");
            builder.append("| 开始时刻 : " + startTime);
            builder.append("\n");
            builder.append("| 等待运行 : " + (runningTime - startTime) + " ms ");
            builder.append("\n");
            builder.append("| 运行耗时 : " + (finishedTime - runningTime) + " ms ");
            builder.append("\n");
            builder.append("| 阻塞事件 : " + (blockTime - finishedTime) + " ms ");
            builder.append("\n");
            builder.append("| 结束时刻 : " + recycledTime);
            builder.append("\n");
            builder.append("| 整个任务耗时 : " + (recycledTime - startTime) + "ms");
            builder.append("\n");
            builder.append("====================================================");
            builder.append("\n");
            return builder.toString();
        }

        private static String getDependenceInfo(@NonNull TaskRuntimeInfo taskRuntimeInfo) {
            StringBuilder stringBuilder = new StringBuilder();
            if (taskRuntimeInfo.dependences != null && !taskRuntimeInfo.dependences.isEmpty()) {
                for (String s : taskRuntimeInfo.dependences) {
                    stringBuilder.append(s + " ");
                }
            }
            return stringBuilder.toString();
        }
    }
}
