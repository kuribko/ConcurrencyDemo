package org.kuribko.concurrent.bank;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Konstantin on 01.10.2016.
 */
public class Account {
    private int balance;
    private String name;
    private Lock lock = new ReentrantLock();
    private AtomicInteger failCounter = new AtomicInteger();

    public Account(String name, int initialBalance){
        this.name = name;
        this.balance = initialBalance;
    }

    public void withdraw(int amount){
//        System.out.println("    Withdraw from "+name+": "+balance+"-"+amount+"="+(balance-amount));
        balance -= amount;

    }

    public void deposit(int amount){
//        System.out.println("    Deposit to "+name+": "+balance+" + "+amount+" = "+(balance+amount));
        balance += amount;
    }

    public void incFailedTranferCounter(){
        failCounter.incrementAndGet();
    }

    public int getFailCounter() {
        return failCounter.get();
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return name + "("+balance+")";
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }
}
