package org.kuribko.concurrent.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockConditionDemo {
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();

    public static void main(String[] args) {

        new Thread(() -> {
                System.out.println("Thread-1 started");
            lock.lock();
            try {
                System.out.println("Thread-1 locked. Awaiting");
                condition.await();
                System.out.println("Thread-1 completed awaiting");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                System.out.println("Thread-1 unlocked and completed");
            }
        }).start();

        sleep(500);

        new Thread(() -> {
            System.out.println("Thread-2 started");
            lock.lock();
            try {
                System.out.println("Thread-2 locked");
                condition.signal();
                System.out.println("Thread-2 sent signal");
                System.out.println("Thread-2 is going to sleep for 2 seconds");
                sleep(2000);
            } finally {
                lock.unlock();
                System.out.println("Thread-2 unlocked and completed");
            }
        }).start();
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
