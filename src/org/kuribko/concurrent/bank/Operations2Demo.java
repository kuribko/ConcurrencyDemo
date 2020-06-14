package org.kuribko.concurrent.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by Konstantin on 02.10.2016.
 */
public class Operations2Demo {


    public static final int TOTAL_TRANSFARES = 10;

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(30);
        List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();

        final Account a = new Account("A", 1000);
        final Account b = new Account("B", 2000);

//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.scheduleAtFixedRate(new Runnable() {
//                                          @Override
//                                          public void run() {
//                                              System.out.println("\nUnsuccessful getLock(): "+(a.getFailCounter()+b.getFailCounter())+"\n");
//                                          }
//                                      }, 1000, 1000, TimeUnit.MILLISECONDS
//
//        );

        Random rnd = new Random();
        int highPriorityCount = 0;
        List<Transfer> transfers = new ArrayList<Transfer>();
        for (int i = 0; i < TOTAL_TRANSFARES; i++) {
            Transfer t;
            if (rnd.nextInt(2) == 1) {
                t = new Transfer(a, b, rnd.nextInt(100), true, i);
                System.out.println("  Created transfer A to B");
                highPriorityCount++;
            } else {
                t = new Transfer(b, a, rnd.nextInt(100), false, i);
                System.out.println("  Created transfer B to A");
            }
            t.threadLocal.set(i);
            transfers.add(t);
        }

        System.out.println("A to B transfers: " + highPriorityCount + "/" + TOTAL_TRANSFARES + "\n");
        CountDownLatch lowPriorityLatch = new CountDownLatch(highPriorityCount);
        for (Transfer t : transfers) {
            t.setLowPriorityLatch(lowPriorityLatch);
        }

        for (Transfer t : transfers) {
            results.add(service.submit(t));
        }

        service.shutdown();

        int successCounter = 0;
        int outOfMoneyCount = 0;
        for (Future<Boolean> result : results) {
            try {
                if (result.get() == true) {
                    successCounter++;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted");
            } catch (ExecutionException e) {
                outOfMoneyCount++;
            }
        }

        System.out.println("\nSuccessful transfers: " + successCounter + "/" + TOTAL_TRANSFARES);
        System.out.println("Insufficient funds transfers: " + outOfMoneyCount);
        System.out.println("Lock errors: " + (a.getFailCounter() + b.getFailCounter()));
    }

}
