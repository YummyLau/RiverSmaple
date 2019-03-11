package com.effective.android.river.interfaces;

import com.effective.android.river.Project;

public interface PojectListener{

    void onStart(Project task);

    void onRunning(Project task);

    void onFinish(Project task);

    void onRecycle(Project task);
}
