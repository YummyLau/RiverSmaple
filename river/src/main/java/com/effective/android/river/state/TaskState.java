package com.effective.android.river.state;

import android.support.annotation.IntDef;

@IntDef({TaskState.IDLE, TaskState.RUNNING, TaskState.FINISHED, TaskState.START})
public @interface TaskState {
    int IDLE = 0;
    int START = 1;
    int RUNNING = 2;
    int FINISHED = 3;
    int RECYCLED = 4;
}
