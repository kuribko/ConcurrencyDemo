package org.kuribko.concurrent.executors;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceDemo {

    public static final int THREAD_TIMEOUT_MILLIS = 500;
    public static final int THREADS_COUNT = 15;
    public static final int THREAD_POOL_SIZE = 3;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        for (int i = 0; i < THREADS_COUNT; i++) {
            executorService.submit(new Task(i + 1));
        }
        executorService.shutdown();
        System.out.println(String.format("executorService.isShutdown() = %b", executorService.isShutdown()));
        System.out.println("Executor service is OFF, it will allow committed threads to end but won't accept new ones");

        System.out.println(String.format("Executor is not terminated at this point as some threads are running: executorService.isTerminated() = %b", executorService.isTerminated()));

        // will throw RejectedExecutionException as shutdown() initiated
//        executorService.submit(new Task(100));

        sleep(THREADS_COUNT / THREAD_POOL_SIZE * THREAD_TIMEOUT_MILLIS + 1000);
        System.out.println(String.format("Executor is now terminated at this point as no threads are running: executorService.isTerminated() = %b", executorService.isTerminated()));

        try {
            // block until all tasks are completed or timeout occurs
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //initialize shutdown and return all queued tasts
        List<Runnable> runnables = executorService.shutdownNow();
        System.out.println("Number of queued tasks that are not completed: "+runnables.size());
    }

    public static class Task implements Runnable {

        private final int number;

        public Task(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            System.out.println(String.format("Thread %d started", number));
            sleep(THREAD_TIMEOUT_MILLIS);
        }
    }

    private static void sleep(int threadTimeoutMillis) {
        try {
            Thread.sleep(threadTimeoutMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
