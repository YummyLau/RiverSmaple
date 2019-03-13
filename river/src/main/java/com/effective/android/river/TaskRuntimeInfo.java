package com.effective.android.river;

import android.util.SparseArray;

import com.effective.android.river.anno.TaskState;

import java.util.Set;

public class TaskRuntimeInfo {

    public String taskName;
    public Set<String> dependences;
    public String threadName;
    public SparseArray<Long> stateTime;
    @TaskState
    public int state = TaskState.IDLE;
    public boolean isProject;
    public long taskResume = 0;             //当前任务消耗时间
    public long dependLinkResume = 0;       //节点存在依赖链中，当前链头到该节点的消耗时间

    private TaskRuntimeInfo() {
        taskName = "";
        dependences = null;
        threadName = "";
        stateTime = new SparseArray<>();
        stateTime.put(TaskState.IDLE, -1L);
        stateTime.put(TaskState.START, -1L);
        stateTime.put(TaskState.RUNNING, -1L);
        stateTime.put(TaskState.FINISHED, -1L);
        isProject = false;
    }

    public static TaskRuntimeInfo create() {
        return new TaskRuntimeInfo();
    }
}
