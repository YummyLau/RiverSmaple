package com.effective.android.river;

import android.support.annotation.NonNull;

public class Project extends Task {

    Task mFinishTask;
    Task mStartTask;

    public Project(String name) {
        super(name);
    }

    public Project(String name, boolean async) {
        super(name, async);
    }

    @Override
    public void behind(Task task) {
        mFinishTask.behind(task);
    }

    @Override
    public void dependOn(Task task) {
        mStartTask.dependOn(task);
    }

    @Override
    public void removeBehind(Task task) {
        mFinishTask.removeBehind(task);
    }

    @Override
    public void removeDependence(Task task) {
        mStartTask.removeDependence(task);
    }

    @Override
    public synchronized void start() {
        mStartTask.start();
    }

    @Override
    public void run(String name) {
        //不需要处理
    }

    public static class Builder {

        private Task mCurrentAddTask;
        private Task mFinishTask;
        private Task mStartTask;
        private boolean mCurrentTaskShouldDependOnStartTask;
        private Project mProject;
        private TaskFactory mFactory;

        public Builder(@NonNull String projectName, @NonNull TaskFactory taskFactory) {
            this.mCurrentAddTask = null;
            this.mCurrentTaskShouldDependOnStartTask = false;
            this.mProject = new Project(projectName);
            this.mStartTask = new CriticalTask(projectName + "_start");
            this.mFinishTask = new CriticalTask(projectName + "_end");
            this.mProject.mStartTask = mStartTask;
            this.mProject.mFinishTask = mFinishTask;
            this.mFactory = taskFactory;
            if (mFactory == null) {
                throw new IllegalArgumentException("taskFactory cant's be null");
            }
        }

        public Project build() {
            if (mCurrentAddTask != null) {
                if (mCurrentTaskShouldDependOnStartTask) {
                    mStartTask.behind(mCurrentAddTask);
                }
            } else {
                mStartTask.behind(mFinishTask);
            }
            return mProject;
        }

        public Builder add(String taskName) {
            return add(mFactory.getTask(taskName));
        }

        public Builder add(Task task) {
            if (mCurrentTaskShouldDependOnStartTask && mCurrentAddTask != null) {
                mStartTask.behind(mCurrentAddTask);
            }
            mCurrentAddTask = task;
            mCurrentTaskShouldDependOnStartTask = true;
            mCurrentAddTask.behind(mFinishTask);
            return this;
        }

        public Builder dependOn(String taskName) {
            return dependOn(mFactory.getTask(taskName));
        }

        public Builder dependOn(Task task) {
            task.behind(mCurrentAddTask);
            mFinishTask.removeDependence(task);
            mCurrentTaskShouldDependOnStartTask = false;
            return this;
        }

        public Builder dependOn(String... names) {
            if (names != null & names.length > 0) {
                for (String name : names) {
                    Task task = mFactory.getTask(name);
                    task.behind(mCurrentAddTask);
                    mFinishTask.removeDependence(task);
                }
                mCurrentTaskShouldDependOnStartTask = false;
            }
            return Builder.this;
        }
    }

    /**
     * 作为临界节点，标识 project 的开始和结束。
     * 同个 project 下可能需要等待 {次后节点们} 统一结束直接才能进入结束节点。
     */
    private static class CriticalTask extends Task {

        public CriticalTask(String name) {
            super(name);
        }

        @Override
        public void run(String name) {

        }
    }
}
