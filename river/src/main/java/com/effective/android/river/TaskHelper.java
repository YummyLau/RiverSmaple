package com.effective.android.river;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskHelper {

    static final Map<String, List<Task>> w = new HashMap<>();

    public static void addWaitTask(Task task, String name) {
        if (!TextUtils.isEmpty(name)) {
            List<Task> tasks = w.get(name);
            if (tasks == null) {
                tasks = new ArrayList<>();
                w.put(name, tasks);
            }
            if (!tasks.contains(task)) {
                task.waitTasks = true;
                tasks.add(task);
            }
        }
    }

    public static void releaseWaitTask(String name) {
        if (!TextUtils.isEmpty(name)) {
            List<Task> tasks = w.get(name);
            if (tasks != null && !tasks.isEmpty()) {
                for (Task task : tasks) {
                    task.waitTasks = false;
                }
                tasks.clear();
            }
        }
    }

    public static void toStart(@NonNull Task task) {
        task.state = TaskState.START;
        TaskInfoCollections.setStateTime(task);
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onStart(task);
        }
    }

    public static void toBlock(@NonNull Task task) {
        task.state = TaskState.BLOCK;
        TaskInfoCollections.setStateTime(task);
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onBlock(task);
        }
    }

    public static void toRunning(@NonNull Task task, String threadName) {
        task.state = TaskState.RUNNING;
        TaskInfoCollections.setStateTime(task);
        TaskInfoCollections.setThreadName(task, threadName);
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onRunning(task);
        }
    }

    public static void toFinish(@NonNull Task task) {
        task.state = TaskState.FINISHED;
        TaskInfoCollections.setStateTime(task);
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onFinish(task);
        }
        releaseWaitTask(task.name);
    }

    public static void toRecycle(@NonNull Task task) {
        task.state = TaskState.RECYCLED;
        TaskInfoCollections.setStateTime(task);
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onRecycled(task);
        }
    }

    public static boolean isTaskIdle(@NonNull Task task) {
        return task.state == TaskState.IDLE;
    }
}
