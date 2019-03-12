package com.effective.android.river;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TaskRuntimeInfo {

    public String taskName;
    public Set<String> dependences;
    public String threadName;
    public SparseArray<Long> stateTime;
    @TaskState
    public int state = TaskState.IDLE;
    public boolean isProject;

    private TaskRuntimeInfo() {
        taskName = "";
        dependences = null;
        threadName = "";
        stateTime = new SparseArray<>();
        stateTime.put(TaskState.IDLE, -1L);
        stateTime.put(TaskState.START, -1L);
        stateTime.put(TaskState.RUNNING, -1L);
        stateTime.put(TaskState.FINISHED, -1L);
        stateTime.put(TaskState.BLOCK, -1L);
        isProject = false;
    }

    public static TaskRuntimeInfo create() {
        return new TaskRuntimeInfo();
    }
}
