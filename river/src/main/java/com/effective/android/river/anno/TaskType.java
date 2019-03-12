package com.effective.android.river.anno;


public @interface TaskType {
    int UI_THREAD = 0;                       //主线程
    int UI_THREAD_BEFORE_ACTIVITY = 0;       //主线程
    int IO_TASK = 1;                         //启动,可能需要等待调度，
    int CPU_TASK = 2;                        //运行
}
