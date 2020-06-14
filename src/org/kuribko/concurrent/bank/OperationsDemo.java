package org.kuribko.concurrent.bank;

import java.util.concurrent.TimeUnit;

public class OperationsDemo {

    private static final long WAIT_SEC = 2;

    public static void main(String[] args) {

        final Account a = new Account("A", 1000);
        final Account b = new Account("B", 2000);

        new Thread(() -> {
            try {
                transfer(a, b, 500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                transfer(b, a, 300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void transfer(Account a, Account b, int amount) throws InterruptedException {
        if (a.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
            System.out.println("  " + a + " is locked. Trying to get lock of " + b);
            try {
                Thread.sleep(100);

                if (b.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("  " + b + " is locked");

                        System.out.println("Transferring " + amount + " from " + a + " to " + b);
                        if (a.getBalance() < amount) {
                            throw new InsufficientFundsException();
                        }

                        a.withdraw(amount);
                        b.deposit(amount);
                    } finally {
                        b.getLock().unlock();
                        System.out.println("  " + b + " is unlocked");
                    }
                } else {
                    System.out.println("Error while waiting lock of " + b);
                    b.incFailedTranferCounter();
                }
            } finally {
                a.getLock().unlock();
                System.out.println("  " + a + " is unlocked");
            }

        } else {
            System.out.println("Error while waiting lock of " + a);
            a.incFailedTranferCounter();
        }
    }

}

