package com.effective.android.river;


public interface ITaskListener {

    void onStart(Task task);

    void onRunning(Task task);

    void onFinish(Task task);
}
