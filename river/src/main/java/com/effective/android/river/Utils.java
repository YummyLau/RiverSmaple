package com.effective.android.river;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Utils {

    private static Comparator<Task> sTaskComparator = new Comparator<Task>() {
        @Override
        public int compare(Task lhs, Task rhs) {
            return lhs.getExecutePriority() - rhs.getExecutePriority();
        }
    };

    public static Comparator<Task> getsTaskComparator() {
        return sTaskComparator;
    }
}
