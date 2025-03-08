package org.opentext.src.impl;

import org.opentext.src.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaskExecuterImpl implements Main.TaskExecutor {

    private final ExecutorService executorService;
    ConcurrentHashMap<String, ReentrantReadWriteLock> groupLocks = new ConcurrentHashMap<>();
    LinkedBlockingQueue<Main.Task<?>> queue = new LinkedBlockingQueue<>();

    public TaskExecuterImpl(int concurrency){
        executorService = Executors.newFixedThreadPool(concurrency);
    }

    @Override
    public <T> Future<T> submitTask(Main.Task<T> task) {
        queue.offer(task);
        return executorService.submit(task.taskAction());
    }

    public void shutDown(){
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdownNow();
        }
    }

    public List<Main.Task<String>> createWriteTasks(int capacity, Main.TaskGroup taskGroup){
        List<Main.Task<String>> tasks = new ArrayList<>();
        for(int i = 1; i <= capacity; i++){
            tasks.add(createWriteTask(UUID.randomUUID(), taskGroup));
        }
        return tasks;
    }

    private Main.Task<String> createReadTask(UUID taskId, Main.TaskGroup taskGroup){
        Main.Task<String> task = new Main.Task<>(taskId, taskGroup, Main.TaskType.READ, () -> {
            ReentrantReadWriteLock lock = groupLocks.computeIfAbsent(taskGroup.groupUUID().toString(), k -> new ReentrantReadWriteLock());
            ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
            ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
            Main.Task<?> queuedTask = queue.poll();
            try {
                if(queuedTask.taskType().equals(Main.TaskType.READ)){
                    readLock.lock();
                    System.out.println("Task " + taskId.toString() + " with group " + taskGroup.groupUUID().toString() + " consuming " + queuedTask.taskUUID().toString() + " with consumed group " + queuedTask.taskGroup().groupUUID().toString() + " performing read operation. ");
                    Thread.sleep(500);
                    return taskId.toString();
                } else {
                    writeLock.lock();
                    System.out.println("Task " + taskId.toString() + " with group " + taskGroup.groupUUID().toString() + " consuming " + queuedTask.taskUUID().toString() + " with consumed group " + queuedTask.taskGroup().groupUUID().toString() + " performing read operation. ");
                    Thread.sleep(500);
                    return taskId.toString();
                }
            } catch (InterruptedException e) {
                return taskId.toString();
            } finally {
                if(queuedTask.taskType().equals(Main.TaskType.READ)){
                    readLock.unlock();
                } else {
                    writeLock.unlock();
                }
            }
        });
        return task;
    }

    public Main.Task<String> createWriteTask(UUID taskId, Main.TaskGroup taskGroup){
        Main.Task<String> task = new Main.Task<>(taskId, taskGroup, Main.TaskType.WRITE, () -> {
            ReentrantReadWriteLock lock = groupLocks.computeIfAbsent(taskGroup.groupUUID().toString(), k -> new ReentrantReadWriteLock());
            ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
            try {
                writeLock.lock();
                System.out.println("Task " + taskId + " with group " + taskGroup.groupUUID().toString() + " performing write operation. ");
                Thread.sleep(500);
                return taskId.toString();
            } catch (InterruptedException e) {
                return taskId.toString();
            } finally {
                writeLock.unlock();
            }
        });
        queue.offer(task);
        return task;
    }

    public List<Main.Task<String>> createReadTasks(int capacity, Main.TaskGroup taskGroup) {
        List<Main.Task<String>> tasks = new ArrayList<>();
        for(int i = 1; i <= capacity; i++){
            tasks.add(createReadTask(UUID.randomUUID(), taskGroup));
        }
        return tasks;
    }
}
