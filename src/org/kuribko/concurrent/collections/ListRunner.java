package org.kuribko.concurrent.collections;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Konstantin on 03.10.2016.
 */
public class ListRunner implements Callable<Long> {

    private List<Integer> list;
    private int fromIndex;
    private int toIndex;
    private CountDownLatch startLatch;

    public ListRunner(List<Integer> list, int fromIndex, int toIndex, CountDownLatch startLatch) {
        this.list = list;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.startLatch = startLatch;
    }

    @Override
    public Long call() throws Exception {
        startLatch.await();
        long t1 = System.nanoTime();
        for (int i = fromIndex; i < toIndex; i++) {
            list.get(i);
//            if(i%10==0){
//                list.add(i);
//            }
        }
        long t2 = System.nanoTime();
        return t2-t1;
    }
}
