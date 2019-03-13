package com.effective.android.river.interfaces;


import com.effective.android.river.Task;

public interface ITaskListener {

    void onStart(Task task);

    void onRunning(Task task);

    void onFinish(Task task);
}
