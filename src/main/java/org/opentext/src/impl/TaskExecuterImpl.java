package org.opentext.src.impl;

import org.opentext.src.Main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TaskExecuterImpl implements Main.TaskExecutor {

    private ExecutorService executorService;

    public TaskExecuterImpl(ExecutorService executorService){
        this.executorService = executorService;
    }

    @Override
    public <T> Future<T> submitTask(Main.Task<T> task) {
        return executorService.submit(task.taskAction());
    }
}
