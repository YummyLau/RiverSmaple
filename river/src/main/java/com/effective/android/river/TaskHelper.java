package com.effective.android.river;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.effective.android.river.anno.TaskState;
import com.effective.android.river.interfaces.ITaskListener;

import java.util.List;

public class TaskHelper {

    private static Handler sHandler = Config.getHandler();
    private static RiverThreadPool riverThreadPool = Config.getThreadPool();


    public static void toStart(@NonNull Task task) {
        task.setState(TaskState.START);
        TaskInfoCollections.setStateInfo(task);
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onStart(task);
        }
    }

    public static void toRunning(@NonNull Task task, String threadName) {
        task.setState(TaskState.RUNNING);
        TaskInfoCollections.setStateInfo(task);
        TaskInfoCollections.setThreadName(task, threadName);
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onRunning(task);
        }
    }

    public static void toFinish(@NonNull Task task) {
        task.setState(TaskState.FINISHED);
        TaskInfoCollections.setStateInfo(task);
        Config.removeWaitTask(task.getId());
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onFinish(task);
        }
    }

    /**
     * 是否是非法的task
     * 如果整个启动流程图中存在两个 task 的 name 一致，则抛出异常
     * 如果 task 在启动的时候状态已经不是 IDLE,则抛出异常
     *
     * @param task
     * @return
     */
    public static void assertTaskWhenStart(@NonNull Task task) throws RuntimeException {
        if (TaskInfoCollections.hasTaskRuntimeInfo(task)) {
            throw new RuntimeException("The entire load map does not allow tasks with the same id (" +task.getId() + ")!");
        }
        if (task.getState() != TaskState.IDLE) {
            throw new RuntimeException("can no run task " + task.getId() + " again!");
        }
    }

    public static void smartRun(@NonNull Task task) {
        if (task.isAsyncTask()) {
            riverThreadPool.executeTask(task);
        } else {
            if (!Config.hasWaitTasks()) {
                sHandler.post(task);
            } else {
                Config.addRunTasks(task);
            }
        }
    }
}
