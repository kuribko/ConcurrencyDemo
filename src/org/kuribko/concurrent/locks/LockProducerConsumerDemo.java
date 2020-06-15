package org.kuribko.concurrent.locks;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockProducerConsumerDemo {

    public static final int PRODUCER_ITERATIONS = 30;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition producedCondition = lock.newCondition();
    private static final Condition consumedCondition = lock.newCondition();
    private static final CopyOnWriteArrayList<Integer> messages = new CopyOnWriteArrayList<>();
    public static final int PRODUCER_DELAY = 300;
    public static final int CONSUMER_DELAY = PRODUCER_DELAY * 2;
    public static final int BUFFER_SIZE = 5;

    public static void main(String[] args) {
        new Thread(() -> {
            for (int i = 0; i < PRODUCER_ITERATIONS; i++) {
                lock.lock();
                try {
                    while (messages.size() >= BUFFER_SIZE) {
                        System.out.println("Producer is full, waiting");
                        consumedCondition.await();
                    }
                    int message = i + 1;
                    System.out.println("Producer created " + message);
                    messages.add(message);
                    producedCondition.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
                sleep(PRODUCER_DELAY);

            }
            System.out.println("Producer competed");
        }).start();

        new Thread(() -> {
            for (int i = 0; i < PRODUCER_ITERATIONS; i++) {
                lock.lock();
                try {
                    while (messages.size() == 0) {
                        System.out.println("Consumer is empty, waiting");
                        producedCondition.await();
                    }
                    Integer message = messages.remove(0);
                    System.out.println("Consumer received " + message);
                    consumedCondition.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
                sleep(CONSUMER_DELAY);
            }
            System.out.println("Consumer completed");
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
