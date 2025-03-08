package org.opentext.src;

import org.opentext.src.impl.TaskExecuterImpl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class Driver {

    public static void main(String[] args) {
        // Assumption for READ/WRITE operation we need synchronization.

        // Concurrency Level 5
        // Implemented Custom executer service with additional methods to create tasks
        TaskExecuterImpl executor = new TaskExecuterImpl(5);

        // Tasks
        // Groups {A, B} Write for Concurrent Write but Sequential for a group
        // Group C represent Sequential Read

        // Group A, B represent Write
        Main.TaskGroup taskGroupA = new Main.TaskGroup(UUID.randomUUID());
        Main.TaskGroup taskGroupB = new Main.TaskGroup(UUID.randomUUID());

        // Group C represent Read
        Main.TaskGroup taskGroupC = new Main.TaskGroup(UUID.randomUUID());

        // Creating Write Tasks with Group A & Group B
        List<Main.Task<String>> writeTasksGroupA = executor.createWriteTasks(2, taskGroupA);
        List<Main.Task<String>> writeTasksGroupB = executor.createWriteTasks(2, taskGroupB);

        // Creating Read Tasks with Group C
        List<Main.Task<String>> readTasksGroupC = executor.createReadTasks(4, taskGroupC);

        writeTasksGroupA.addAll(writeTasksGroupB);
        writeTasksGroupA.addAll(readTasksGroupC);

        // Submit all READ & WRITE tasks
        List<Future<String>> futureList = writeTasksGroupA.stream().map(executor::submitTask).toList();

        futureList.forEach(f -> {
            try {
                System.out.println("Task " + f.get() + " is completed");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // stopping executer service
        executor.shutDown();
    }
}
