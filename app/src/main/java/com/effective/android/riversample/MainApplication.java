package com.effective.android.riversample;

import android.app.Application;

import com.effective.android.river.Logger;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("MainApplication  start");
        TaskTest taskTest = new TaskTest();
        taskTest.start();
        Logger.d("MainApplication  end");
    }
}
