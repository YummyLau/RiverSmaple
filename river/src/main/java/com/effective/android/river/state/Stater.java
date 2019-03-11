package com.effective.android.river.state;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.effective.android.river.Task;
import com.effective.android.river.debug.Timer;
import com.effective.android.river.interfaces.TaskListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stater {

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
        Timer.record(task);
        List<TaskListener> taskListeners = task.getTaskListeners();
        for (TaskListener listener : taskListeners) {
            listener.onStart(task);
        }
    }

    public static void toRuning(@NonNull Task task) {
        task.state = TaskState.RUNNING;
        Timer.record(task);
        List<TaskListener> taskListeners = task.getTaskListeners();
        for (TaskListener listener : taskListeners) {
            listener.onRunning(task);
        }
    }

    public static void toFinish(@NonNull Task task) {
        task.state = TaskState.FINISHED;
        Timer.record(task);
        List<TaskListener> taskListeners = task.getTaskListeners();
        for (TaskListener listener : taskListeners) {
            listener.onFinish(task);
        }
        releaseWaitTask(task.name);
    }

    public static void toRecycle(@NonNull Task task){
        task.state = TaskState.RECYCLED;
        Timer.record(task);
        List<TaskListener> taskListeners = task.getTaskListeners();
        for (TaskListener listener : taskListeners) {
            listener.onRecycle(task);
        }
    }

    public static boolean isIdelTask(@NonNull Task task) {
        return task.state == TaskState.IDLE;
    }

    public static boolean isRunningTask(@NonNull Task task) {
        return task.state == TaskState.RUNNING;
    }

    public static boolean isFinishedTask(@NonNull Task task) {
        return task.state == TaskState.FINISHED;
    }
}
