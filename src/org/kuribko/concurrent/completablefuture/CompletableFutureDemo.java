package org.kuribko.concurrent.completablefuture;

import org.kuribko.concurrent.util.ConsoleColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CompletableFutureDemo {

    public static final int ORDER_COUNT = 8;
    public static final int REPORT_DELAY = 1000;
    public static final int CREATION_DELAY = 1000;
    public static final int ENRICHMENT_DELAY = 300;
    public static final int CPU_THREADS_COUNT = 2;
    public static final String CURRENT_ORDER_COLOR = ConsoleColors.PURPLE;
    public static final String CURRENT_STATUS_COLOR = ConsoleColors.RED;
    public static final String CURRENT_METRICS_COLOR = ConsoleColors.CYAN;

    private static final List<Order> orders = new CopyOnWriteArrayList();
    private static final ReentrantLock ordersLock = new ReentrantLock();

    public static void main(String[] args) {
        ExecutorService cpuBoundExecutor = Executors.newFixedThreadPool(CPU_THREADS_COUNT);
        ExecutorService ioBoundExecutor = Executors.newCachedThreadPool();
        CompletableFuture[] completableFutures = new CompletableFuture[ORDER_COUNT];

        for (int i = 0; i < ORDER_COUNT; i++) {
            completableFutures[i] = CompletableFuture.supplyAsync(() -> getOrder(), ioBoundExecutor)
                    .thenApplyAsync(order -> enrichOrder(order), cpuBoundExecutor)
                    .thenApplyAsync(order -> performPayment(order), ioBoundExecutor)
                    .thenAccept(order -> sendEmail(order));

        }

        CompletableFuture.allOf(completableFutures).join();
        cpuBoundExecutor.shutdown();
        ioBoundExecutor.shutdown();
    }

    private static Order getOrder() {
        Order order = new Order();
        orders.add(order);
        changeStatus(order, Status.NEW);

        sleep(new Random().nextInt(CREATION_DELAY * 4) + CREATION_DELAY);

        changeStatus(order, Status.CREATED);
        return order;
    }

    private static void changeStatus(Order order, Status status) {
//        ordersLock.lock();
//        try {
            order.addStatus(status);
            report(order, status);
            sleep(new Random().nextInt(REPORT_DELAY));
//        } finally {
//            ordersLock.unlock();
//        }

    }

    private static void report(Order order, Status currentStatus) {
        System.out.println(getMetrics(currentStatus) + "             " + getStatuses(order, currentStatus));
    }

    private static Order enrichOrder(Order order) {
        changeStatus(order, Status.ENRICHED);
        sleep(new Random().nextInt(ENRICHMENT_DELAY));
        return order;
    }

    private static Order performPayment(Order order) {
        changeStatus(order, Status.PAID);
        return order;
    }

    private static void sendEmail(Order order) {
        changeStatus(order, Status.EMAIL_SENT);
    }

    private static String getMetrics(Status currentStatus) {
        return Arrays.stream(Status.values())
                .map(status -> getSingleMetrics(status, currentStatus))
                .collect(Collectors.joining(" "));
    }

    private static String getSingleMetrics(Status status, Status currentStatus) {
        long value = orders.stream().filter(o -> o.statuses.contains(status)).count();
        if (status == currentStatus) {
            return String.format("%s%s=%d%s", CURRENT_METRICS_COLOR, status.shortName(), value, ConsoleColors.RESET);
        } else {
            return String.format("%s=%d", status.shortName(), value);
        }
    }

    private static String getStatuses(Order currentOrder, Status currentStatus) {
        StringBuilder sb = new StringBuilder();
        String statuses = "";
        for (Order order : orders) {
            if (order == currentOrder) {
                sb.append(CURRENT_ORDER_COLOR);
                statuses = order.statuses.stream().map(status -> {
                    String value = status.shortName();
                    if (status == currentStatus) {
                        value = CURRENT_STATUS_COLOR + value + CURRENT_ORDER_COLOR;
                    }
                    return value;
                }).collect(Collectors.joining(" "));
                statuses = statuses + ConsoleColors.RESET;
            } else {
                statuses = order.statuses.stream().map(Status::shortName)
                        .collect(Collectors.joining(" "));
            }
            sb.append(statuses);
            int statusSymbols = order.statuses.size() == 0 ? 0 : order.statuses.size() * 2 - 1;
            for (int j = statusSymbols; j < 13; j++) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    public static class Order {
        private final List<Status> statuses = new ArrayList<>();

        public void addStatus(Status status) {
            statuses.add(status);
        }

        @Override
        public String toString() {
            String shortStatuses = statuses.stream().map(status -> String.valueOf(status.name().charAt(0))).collect(Collectors.joining(" "));
            return String.format("%-12s", shortStatuses);
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private enum Status {
        NEW, CREATED, ENRICHED, PAID, EMAIL_SENT;

        public String shortName() {
            return String.valueOf(this.name().charAt(0));
        }

    }
}
