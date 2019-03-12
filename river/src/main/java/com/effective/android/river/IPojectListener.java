package com.effective.android.river;


public interface IPojectListener {

    void onStart(Project task);

    void onRunning(Project task);

    void onFinish(Project task);

    void onRecycle(Project task);
}
