package org.kuribko.concurrent.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Konstantin on 03.10.2016.
 */
public class Main {

    private static ExecutorService service = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {
        System.out.println("Concurrent run through synchronizedList and CopyOnWriteArrayList");
        List<Integer> list1 = Collections.synchronizedList(new ArrayList<Integer>());
        List<Integer> list2 = new CopyOnWriteArrayList<>();

        fillList(list1);
        fillList(list2);

        System.out.println("Synchronized list: ");
        checkList(list1);

        System.out.println("CopyOnWriteArrayList list: ");
        checkList(list2);

        service.shutdown();
    }

    private static void checkList(List<Integer> list) {
        CountDownLatch latch = new CountDownLatch(1);

        Future<Long> duration1 = service.submit(new ListRunner(list, 0, 50, latch));
        Future<Long> duration2 = service.submit(new ListRunner(list, 50, 100, latch));

        latch.countDown();

        try {
            System.out.println((duration1.get()+duration2.get())/1000);
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void fillList(List<Integer> list) {
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
    }
}
