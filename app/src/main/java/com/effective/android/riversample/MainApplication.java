package com.effective.android.riversample;

import android.app.Application;

import com.effective.android.river.Logger;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("详情信息 MainApplication  start");
        TaskTest taskTest = new TaskTest();
        taskTest.start();
        Logger.d("详情信息 MainApplication  end");
    }
}
