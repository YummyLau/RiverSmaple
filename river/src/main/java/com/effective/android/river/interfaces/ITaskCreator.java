package com.effective.android.river.interfaces;

import android.support.annotation.Nullable;

import com.effective.android.river.Task;

public interface ITaskCreator {

    @Nullable
    Task createTask(String taskName);
}
