package com.effective.android.river;

import java.util.HashMap;
import java.util.Map;

/**
 * 收集 task 流程中的信息
 * created by yummylau on 2019/03/12
 */
public class TaskInfoCollections {

    private static final Map<String, TaskRuntimeInfo> sInfo = new HashMap<>();

    public static boolean hasTaskRuntimeInfo(Task task) {
        if (task == null) {
            return false;
        }
        return sInfo.get(task.getId()) != null;
    }

    public static TaskRuntimeInfo getTaskRuntimeInfo(Task task) {
        if (task == null) {
            return null;
        }
        TaskRuntimeInfo taskRuntimeInfo = sInfo.get(task.getId());
        if (taskRuntimeInfo == null) {
            taskRuntimeInfo = TaskRuntimeInfo.create();
            taskRuntimeInfo.dependences = task.getDependTaskName();
            taskRuntimeInfo.isProject = task instanceof Project;
            sInfo.put(task.getId(), taskRuntimeInfo);
        }
        return taskRuntimeInfo;
    }

    public static void setThreadName(Task task, String threadName) {
        TaskRuntimeInfo taskRuntimeInfo = getTaskRuntimeInfo(task);
        if (taskRuntimeInfo != null) {
            taskRuntimeInfo.threadName = threadName;
        }
    }

    public static void setStateInfo(Task task) {
        TaskRuntimeInfo taskRuntimeInfo = getTaskRuntimeInfo(task);
        if (taskRuntimeInfo != null) {
            taskRuntimeInfo.state = task.getState();
            taskRuntimeInfo.stateTime.put(task.getState(), System.currentTimeMillis());
        }
    }
}
