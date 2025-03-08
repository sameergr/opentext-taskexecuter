package org.opentext.src;

import org.opentext.src.impl.TaskExecuterImpl;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Driver {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Main.TaskExecutor executor = new TaskExecuterImpl(executorService);

        Main.TaskGroup taskGroupA = new Main.TaskGroup(UUID.fromString("A"));
        Main.TaskGroup taskGroupB = new Main.TaskGroup(UUID.fromString("B"));
//        Main.TaskGroup taskGroupC = new Main.TaskGroup(UUID.fromString("C"));

        Future<Boolean> result1 = executor.submitTask(new Main.Task<Boolean>(UUID.fromString("1"), taskGroupA, Main.TaskType.WRITE, () -> {
            System.out.println("Executed Task 1");
            return true;
        }));

        Future<Boolean> result2 = executor.submitTask(new Main.Task<Boolean>(UUID.fromString("2"), taskGroupA, Main.TaskType.WRITE, () -> {
            System.out.println("Executed Task 1");
            return true;
        }));

        Future<Boolean> result3 = executor.submitTask(new Main.Task<Boolean>(UUID.fromString("3"), taskGroupB, Main.TaskType.WRITE, () -> {
            System.out.println("Executed Task 1");
            return true;
        }));



    }
}
