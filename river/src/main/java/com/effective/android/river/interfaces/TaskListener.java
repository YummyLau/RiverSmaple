package com.effective.android.river.interfaces;

import com.effective.android.river.Task;

public interface TaskListener {

    void onStart(Task task);

    void onRunning(Task task);

    void onFinish(Task task);

    void onRecycle(Task task);
}
