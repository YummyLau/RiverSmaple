package com.effective.android.riversample;

import android.app.Application;
import android.util.Log;

import com.effective.android.river.debug.Logger;

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
