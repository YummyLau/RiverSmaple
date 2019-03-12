package com.effective.android.river;

import android.support.annotation.NonNull;

import com.effective.android.river.anno.TaskState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskHelper {

    static final Map<String, List<Task>> w = new HashMap<>();


    public static void toStart(@NonNull Task task) {
        task.state = TaskState.START;
        TaskInfoCollections.setStateTime(task);
        List<ITaskListener> taskListeners = task.getTaskListeners();
        for (ITaskListener listener : taskListeners) {
            listener.onStart(task);
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
    }

    public static boolean isTaskIdle(@NonNull Task task) {
        return task.state == TaskState.IDLE;
    }
}
