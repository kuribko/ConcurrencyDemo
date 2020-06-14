package org.kuribko.concurrent.locks;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedVsReentrantLockDemo {

    public static final int MAX_VALUE = 500;
    public static final int TIMEOUT_MILLIS = 20;
    private static final AtomicInteger syncValue = new AtomicInteger(0);
    private static final Object monitor = new Object();
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final AtomicInteger lockValue = new AtomicInteger(0);
    private static final ReentrantLock reentrantLock = new ReentrantLock();


    public static void main(String[] args) {

        long t1 = runConcurrently(() -> incInSynchronized("t1"),
                () -> incInSynchronized("t2"),
                "incrementing synchronized volatile value by 2 threads");

        System.out.println("\n");

        long t2 = runConcurrently(() -> incWithReentrantLocl("t1"), () -> incWithReentrantLocl("t2"), "incrementing atomic integer by 2 threads");
        System.out.println(String.format("\n%d increments via Synchronized=%d ms, ReentrantLock=%d ms", 500, t1, t2));
    }

    private static long runConcurrently(Callable<Long> callable1, Callable<Long> callable2, String text) {
        System.out.println(String.format("Start %s", text));
        Future<Long> task1 = executor.submit(callable1);
        Future<Long> task2 = executor.submit(callable2);

        long result = -1;
        try {
            result = task1.get() + task2.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("\nDuration of %s: %d ms", text, result));
        return result;
    }

    private static Long incInSynchronized(String name) {
        long t1 = System.currentTimeMillis();
        while (syncValue.get() < MAX_VALUE) {
            synchronized (monitor) {
                syncValue.incrementAndGet();
                System.out.print(String.format("%d (%s)  ", syncValue.get(), name));
                if (syncValue.get() % 20 == 0) {
                    System.out.println();
                }
            }
            sleep();
        }
        return System.currentTimeMillis() - t1;
    }

    private static void sleep() {
        try {
            Thread.sleep(TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Long incWithReentrantLocl(String name) {
        long t1 = System.currentTimeMillis();
        while (lockValue.get() < MAX_VALUE) {
            if (reentrantLock.tryLock()) {
                lockValue.incrementAndGet();
                System.out.print(String.format("%d (%s)  ", lockValue.get(), name));
                if (lockValue.get() % 20 == 0) {
                    System.out.println();
                }
                reentrantLock.unlock();

                sleep();
            }
        }
        return System.currentTimeMillis() - t1;
    }


}
