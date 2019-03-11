package com.effective.android.river.debug;

import android.support.annotation.NonNull;

import com.effective.android.river.Config;
import com.effective.android.river.Project;
import com.effective.android.river.Task;
import com.effective.android.river.state.TaskState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * created by yummylau on 2019/03/11
 */
public class Timer {

    public static final Map<String, Map<Integer, Long>> taskTimeInfo = new HashMap<>();

    public static void record(Task task) {
        if (Config.isDebug() && task != null) {
            Map<Integer, Long> map = getTaskInfo(task);
            map.put(task.state, System.currentTimeMillis());
        }
    }

    @NonNull
    private static Map<Integer, Long> getTaskInfo(@NonNull Task task) {
        Map<Integer, Long> map = taskTimeInfo.get(task.name);
        if (map == null) {
            map = new HashMap<>();
            taskTimeInfo.put(task.name, map);
        }
        return map;
    }

    public static String getDependences(@NonNull Task task) {
        StringBuilder stringBuilder = new StringBuilder();
        if (task != null) {
            Set<String> dependences = task.getDependTaskName();
            if (!dependences.isEmpty()) {
                for (String s : dependences) {
                    stringBuilder.append(s + " ");
                }
            }
        }
        return stringBuilder.toString();
    }

    private static final String TASK_INFO = "startTime(%s) endTime(%s) total(%sms)";

    public static String getTaskLifeInfo(@NonNull Task task) {
        Map<Integer, Long> map = getTaskInfo(task);
        long startTime = map.get(TaskState.START);
        long runningTime = map.get(TaskState.RUNNING);
        long finishedTime = map.get(TaskState.FINISHED);
        long recycledTime = map.get(TaskState.RECYCLED);
        boolean isProject = task instanceof Project;
        String name = isProject ? "project" : "task";
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("=======================" + name + "( " + task.name + " )=============================");
        builder.append("\n");
        builder.append("| depend on : " + getDependences(task));
        builder.append("\n");
        String formattedMsg = String.format(TASK_INFO, startTime, recycledTime, recycledTime - startTime);
        builder.append("| " + formattedMsg);
        builder.append("\n");
        builder.append("| wait for running : " + (runningTime - startTime) + " ms ");
        builder.append("\n");
        builder.append("| running resume : " + (finishedTime - runningTime) + " ms ");
        builder.append("\n");
        builder.append("| wait for release : " + (recycledTime - finishedTime) + " ms ");
        builder.append("\n");
        builder.append("====================================================");
        builder.append("\n");
        return builder.toString();
    }

}
