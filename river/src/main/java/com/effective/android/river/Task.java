package com.effective.android.river;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.effective.android.river.anno.TaskState;

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
    protected int state = TaskState.IDLE;
    protected String name;
    protected boolean async;

    private List<Task> behindTasks = new ArrayList<>();                                //被依赖者
    private Set<Task> dependTasks = new HashSet<>();                                   //依赖者
    private Set<String> dependTaskName = new HashSet<>();                              //用于log统计

    protected static final int DEFAULT_EXECUTE_PRIORITY = 0;
    private int mExecutePriority = DEFAULT_EXECUTE_PRIORITY;
    private List<ITaskListener> taskListeners = new ArrayList<>();

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    protected int getExecutePriority() {
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


    protected synchronized void start() {
        if (!TaskHelper.isTaskIdle(this)) {
            throw new RuntimeException("You try to run task " + name + " twice, is there a circular dependency?");
        }
        TaskHelper.toStart(this);
        if (async) {
            sExecutorService.execute(this);
        } else {
            if(RiverManager.sInstance.waitTasks.isEmpty()){
                sHandler.post(this);
            }else{
                RiverManager.sInstance.toRunTask.add(this);
            }
        }
    }

    @Override
    public void run() {
        TaskHelper.toRunning(this, Thread.currentThread().getName());
        run(name);
        TaskHelper.toFinish(this);
        RiverManager.sInstance.waitTasks.remove(this);
        notifyNextTask();
        recycle();
    }

    protected abstract void run(String name);

    public Set<String> getDependTaskName() {
        return dependTaskName;
    }


    /**
     * 后置触发, 和 {@link Task#dependOn(Task)} 方向相反，都可以设置依赖关系
     *
     * @param task
     */
    protected void behind(Task task) {
        if (task != null && task != this) {
            if (task instanceof Project) {
                task = ((Project) task).getStartTask();
            }
            behindTasks.add(task);
            task.dependOn(this);
        }
    }

    protected void removeBehind(Task task) {
        if (task != null && task != this) {
            if (task instanceof Project) {
                task = ((Project) task).getStartTask();
            }
            behindTasks.remove(task);
            task.removeDependence(this);
        }
    }

    /**
     * 前置条件, 和 {@link Task#behind(Task)} 方向相反，都可以设置依赖关系
     *
     * @param task
     */
    public void dependOn(Task task) {
        if (task != null && task != this) {
            if (task instanceof Project) {
                task = ((Project) task).getEndTask();
            }
            dependTasks.add(task);
            dependTaskName.add(task.name);
            //防止外部所有直接调用dependOn无法构建完整图
            if (!task.behindTasks.contains(this)) {
                task.behindTasks.add(this);
            }
        }
    }

    protected void removeDependence(Task task) {
        if (task != null && task != this) {
            if (task instanceof Project) {
                task = ((Project) task).getEndTask();
            }
            dependTasks.remove(task);
            dependTaskName.add(task.name);
            if (task.behindTasks.contains(this)) {
                task.behindTasks.remove(this);
            }
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
            Logger.d("详情信息" + getTaskRuntimeInfoString(task));
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
            builder.append("| 结束时刻 : " + finishedTime);
            builder.append("\n");
            builder.append("| 整个任务耗时 : " + (finishedTime - startTime) + "ms");
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
