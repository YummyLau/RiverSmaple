package com.effective.android.river;


public interface ITaskListener {

    void onStart(Task task);

    void onRunning(Task task);

    void onFinish(Task task);

    void onBlock(Task task);

    void onRecycled(Task task);
}
