package com.effective.android.river.debug;

import com.effective.android.river.Task;
import com.effective.android.river.interfaces.TaskListener;

public class DefaultTaskListener implements TaskListener {

    @Override
    public void onStart(Task task) {
        Logger.d(task.name + " -- onStart -- ");
    }

    @Override
    public void onRunning(Task task) {
        Logger.d(task.name + " -- onRunning -- ");
    }

    @Override
    public void onFinish(Task task) {
        Logger.d(task.name + " -- onFinish -- ");
    }

    @Override
    public void onRecycle(Task task) {
        Logger.d(task.name + " -- onRecycle -- ");
        Logger.d(Logger.TASK_INFO, "task_info :" + Timer.getTaskLifeInfo(task));
    }
}
