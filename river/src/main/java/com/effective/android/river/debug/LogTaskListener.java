package com.effective.android.river.debug;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.effective.android.river.interfaces.ITaskListener;
import com.effective.android.river.Task;
import com.effective.android.river.TaskInfoCollections;
import com.effective.android.river.TaskRuntimeInfo;
import com.effective.android.river.anno.TaskState;

public class LogTaskListener implements ITaskListener {

    @Override
    public void onStart(Task task) {
        Logger.d(task.getId() + " -- onStart -- ");
    }

    @Override
    public void onRunning(Task task) {
        Logger.d(task.getId() + " -- onRunning -- ");
    }

    @Override
    public void onFinish(Task task) {
        Logger.d(task.getId() + " -- onFinish -- ");
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
        builder.append("=======================" + (taskRuntimeInfo.isProject ? "project" : "task") + "( " + task.getId() + " )=============================");
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
