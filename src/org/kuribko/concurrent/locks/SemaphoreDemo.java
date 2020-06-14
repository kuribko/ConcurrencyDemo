package org.kuribko.concurrent.locks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Semaphore allows to run N threads at a time simultaneously
 */
public class SemaphoreDemo {
    public static final int TOTAL_THREADS = 30;
    private static final AtomicInteger threadCounter = new AtomicInteger(0);
    private static Semaphore semaphore = new Semaphore(3);
    private static ExecutorService executorService = Executors.newFixedThreadPool(50);

    public static void main(String[] args) {
        for (int i = 0; i < TOTAL_THREADS; i++) {
            executorService.submit(new Task(semaphore, i));
        }
        executorService.shutdown();
    }

    public static class Task implements Runnable {

        private final Semaphore semaphore;
        private final int number;

        public Task(Semaphore semaphore, int number) {
            this.semaphore = semaphore;
            this.number = number;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                System.out.println(String.format("T-%d started",  number));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(String.format("Completed %d/%d", threadCounter.incrementAndGet(), TOTAL_THREADS));
            semaphore.release();
        }
    }
}
