package com.effective.android.riversample;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.effective.android.river.Project;
import com.effective.android.river.Task;
import com.effective.android.river.TaskFactory;
import com.effective.android.river.ITaskCreator;

import java.util.Random;

public class TaskTest {

    public static final String TASK_A = "task_A";
    public static final String TASK_B = "task_B";
    public static final String TASK_C = "task_C";
    public static final String TASK_D = "task_D";
    public static final String TASK_E = "task_E";

    public void start() {
        final TaskFactory factory = new TaskFactory(new ITaskCreator() {
            @Nullable
            @Override
            public Task createTask(String taskName) {
                if (!TextUtils.isEmpty(taskName)) {
                    switch (taskName) {
                        case TASK_A: {
                            return new TaskA();
                        }
                        case TASK_B: {
                            return new TaskB();
                        }
                        case TASK_C: {
                            return new TaskC();
                        }
                        case TASK_D: {
                            return new TaskD();
                        }
                        case TASK_E: {
                            return new TaskE();
                        }
                    }
                }
                return null;
            }
        });
        TaskD taskD = new TaskD();

        Project.Builder builder = new Project.Builder("project1", factory);
        builder.add(TASK_A);
        builder.add(TASK_B).dependOn(TASK_A);
        builder.add(TASK_C).dependOn(TASK_A);
        Project project = builder.build();
        project.dependOn(taskD);



        TaskE taskE = new TaskE();
        taskE.dependOn(project);

        taskD.start();
    }

    public static class TaskA extends Task {

        public TaskA() {
            super(TASK_A, true);
        }

        @Override
        public void run(String name) {
            try {
                doJob(400);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class TaskB extends Task {

        public TaskB() {
            super(TASK_B);
        }

        @Override
        public void run(String name) {
            try {
                doJob(400);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class TaskC extends Task {

        public TaskC() {
            super(TASK_C, true);
        }

        @Override
        public void run(String name) {
            try {
                doJob(400);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class TaskD extends Task {

        public TaskD() {
            super(TASK_D);
        }

        @Override
        public void run(String name) {
            try {
                doJob(400);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class TaskE extends Task {

        public TaskE() {
            super(TASK_E, true);
        }

        @Override
        public void run(String name) {
            try {
                doJob(400);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void doJob(long millis) {
        long nowTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < nowTime + millis) {
            //程序阻塞指定时间
            int min = 10;
            int max = 99;
            Random random = new Random();
            int num = random.nextInt(max) % (max - min + 1) + min;
        }
    }
}
