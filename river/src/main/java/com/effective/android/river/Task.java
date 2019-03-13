package com.effective.android.river;

import android.os.Build;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.effective.android.river.interfaces.ITaskListener;
import com.effective.android.river.anno.TaskState;
import com.effective.android.river.debug.LogTaskListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * created by yummylau on 2019/03/11
 */
public abstract class Task implements Runnable, Comparable<Task> {

    @TaskState
    private int mState = TaskState.IDLE;           //状态
    private String mId;                            //mId,唯一存在
    private boolean isAsyncTask;                   //是否是异步存在
    private int mPriority;                         //优先级，数值越低，优先级越低

    public static final int DEFAULT_PRIORITY = 0;
    private List<Task> behindTasks = new ArrayList<>();                                //被依赖者
    private Set<Task> dependTasks = new HashSet<>();                                   //依赖者
    private Set<String> dependTaskName = new HashSet<>();                              //用于log统计
    private List<ITaskListener> taskListeners = new ArrayList<>();                     //监听器


    public Task(String id) {
        this(id, false);
    }

    public Task(String id, boolean async) {
        this.mId = id;
        this.isAsyncTask = async;
        this.mPriority = DEFAULT_PRIORITY;
        if (TextUtils.isEmpty(id)) {
            throw new IllegalArgumentException("task's mId can't be empty");
        }
        if (Config.isDebug()) {
            addTaskListener(new LogTaskListener());
        }
    }

    public String getId() {
        return mId;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
    }

    public int getPriority() {
        return mPriority;
    }

    public boolean isAsyncTask() {
        return isAsyncTask;
    }

    public int getState() {
        return mState;
    }

    public void setState(@TaskState int state) {
        this.mState = state;
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
        TaskHelper.assertTaskWhenStart(this);
        TaskHelper.toStart(this);
        TaskHelper.smartRun(this);
    }

    @Override
    public void run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.beginSection(mId);
        }
        TaskHelper.toRunning(this, Thread.currentThread().getName());
        run(mId);
        TaskHelper.toFinish(this);
        notifyBehindTasks();
        recycle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }
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
            dependTaskName.add(task.mId);
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
            dependTaskName.add(task.mId);
            if (task.behindTasks.contains(this)) {
                task.behindTasks.remove(this);
            }
        }
    }

    @Override
    public int compareTo(@NonNull Task o) {
        if (getPriority() < o.getPriority()) {
            return 1;
        }
        if (getPriority() > o.getPriority()) {
            return -1;
        }
        return 0;
    }


    /**
     * 通知后置者自己已经完成了
     */
    void notifyBehindTasks() {
        if (!behindTasks.isEmpty()) {

            if (behindTasks.size() > 1) {
                Collections.sort(behindTasks, Config.getTaskComparator());
            }

            //遍历记下来的任务，通知它们说存在的前置已经完成
            for (Task task : behindTasks) {
                task.dependTaskFinish(this);
            }
        }
    }

    /**
     * 依赖的任务已经完成
     * 比如 B -> A (B 依赖 A), A 完成之后调用该方法通知 B "A依赖已经完成了"
     * 当且仅当 B 的所有依赖都已经完成了, B 开始执行
     *
     * @param dependTask
     */
    void dependTaskFinish(Task dependTask) {

        if (dependTasks.isEmpty()) {
            return;
        }
        dependTasks.remove(dependTask);

        //所有前置任务都已经完成了
        if (dependTasks.isEmpty()) {
            start();
        }
    }

    void recycle() {
        dependTasks.clear();
        behindTasks.clear();
        dependTaskName.clear();
        taskListeners.clear();
    }

}
