package com.effective.android.river;

import java.util.HashMap;
import java.util.Map;

public class TaskFactory {

    private Map<String, Task> mTasks = new HashMap<>();
    private ITaskCreator mTaskCreator;

    public TaskFactory(ITaskCreator creator) {
        mTaskCreator = creator;
    }

    public synchronized Task getTask(String taskName) {
        Task task = mTasks.get(taskName);

        if (task != null) {
            return task;
        }
        task = mTaskCreator.createTask(taskName);

        if (task == null) {
            throw new IllegalArgumentException("Create task fail, there is no task corresponding to the task name. Make sure you have create a task instance in TaskCreator.");
        }

        mTasks.put(taskName, task);
        return task;
    }
}
