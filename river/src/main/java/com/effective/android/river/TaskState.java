package com.effective.android.river;

import android.support.annotation.IntDef;

@IntDef({TaskState.IDLE, TaskState.RUNNING, TaskState.FINISHED, TaskState.START, TaskState.BLOCK, TaskState.RECYCLED})
public @interface TaskState {
    int IDLE = 0;               //静止
    int START = 1;              //启动,可能需要等待调度，
    int RUNNING = 2;            //运行
    int FINISHED = 3;           //运行结束
    int BLOCK = 4;              //阻塞
    int RECYCLED = 5;           //回收
}
