package com.effective.android.river;

import android.util.Log;

/**
 * created by yummylau on 2019/03/11
 */
public class Logger {

    public static final String TASK_INFO = "task_info";
    public static final String TAG = "--River--";

    public static void d(String tag, Object obj) {
        if (Config.isDebug()) {
            Log.d(tag, obj.toString());
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (Config.isDebug()) {
            String formattedMsg = String.format(msg, args);
            Log.d(tag, formattedMsg);
        }
    }

    public static void d(String msg, Object... args) {
        d(TAG, msg, args);
    }

    public static void e(String tag, Object obj) {
        if (Config.isDebug()) {
            Log.e(tag, obj.toString());
        }
    }


    public static void e(String tag, String msg, Object... args) {
        if (Config.isDebug()) {
            String formattedMsg = String.format(msg, args);
            Log.e(tag, formattedMsg);
        }
    }


    public static void i(String tag, Object obj) {
        if (Config.isDebug()) {
            Log.i(tag, obj.toString());
        }
    }

    public static void w(Exception e) {
        if (Config.isDebug()) {
            e.printStackTrace();
        }
    }

    public static void print(Object msg) {
        d(TAG, msg);
    }

    public static void print(String msg, Object... args) {
        d(TAG, msg, args);
    }
}
