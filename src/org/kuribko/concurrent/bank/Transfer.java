package org.kuribko.concurrent.bank;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Konstantin on 02.10.2016.
 */
public class Transfer implements Callable<Boolean> {
    private static final long WAIT_SEC = 2;
    private Account fromAccount;
    private Account toAccount;
    private int amount;
    private boolean highPriority;
    private int id;
    private CountDownLatch lowPriorityLatch;
    public static ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>(){
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public Transfer(Account fromAccount, Account toAccount, int amount, boolean highPriority, int id) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.highPriority = highPriority;
        this.id = id;

    }

    public boolean isHighPriority() {
        return highPriority;
    }

    public void setLowPriorityLatch(CountDownLatch lowPriorityLatch) {
        this.lowPriorityLatch = lowPriorityLatch;
    }

    @Override
    public Boolean call() throws Exception {
        this.threadLocal.set(id);

        if (!isHighPriority()) {
            lowPriorityLatch.await();
        }

        boolean success = false;
        while (!success) {
            if (fromAccount.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
//                System.out.println("\nStarting Transfer");
//                System.out.println("  " + fromAccount + " is locked. Trying to get lock of " + toAccount);
                try {
                    Thread.sleep(100);

                    if (toAccount.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                        try {
//                            System.out.println("  " + toAccount + " is locked");

                            System.out.println("  Transfer #"+ threadLocal.get()+": " + amount + " from " + fromAccount + " to " + toAccount);
                            if (fromAccount.getBalance() < amount) {
                                System.out.println("Error: Insufficient Funds");
                                throw new InsufficientFundsException();
                            }

                            fromAccount.withdraw(amount);
                            toAccount.deposit(amount);

                            int sleepPeriod = new Random().nextInt(1000);
//                        System.out.println("\nSleeping for "+sleepPeriod+" ms");
                            Thread.sleep(sleepPeriod);
//                        System.out.println("Transfer sleep completed\n\n");

//                            System.out.println("Transfer completed");
                            success = true;
                            if(isHighPriority()){
                                lowPriorityLatch.countDown();
                            }
                            return true;
                        } finally {
                            toAccount.getLock().unlock();
//                            System.out.println("  " + toAccount + " is unlocked");
                        }
                    } else {
//                        System.out.println("Error while waiting lock of " + toAccount);
                        toAccount.incFailedTranferCounter();
                        continue;
//                        return false;
                    }
                } finally {
                    fromAccount.getLock().unlock();
//                    System.out.println("  " + fromAccount + " is unlocked");
                }

            } else {
//                System.out.println("Error while waiting lock of " + toAccount);
                toAccount.incFailedTranferCounter();
                continue;
//                return false;
            }
        }
        return true;
    }
}
