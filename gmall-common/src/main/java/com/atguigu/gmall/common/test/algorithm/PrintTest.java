package com.atguigu.gmall.common.test.algorithm;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PrintTest {
    public static void main(String[] args) {
        Dome dome = new Dome();
        new Thread(() ->{
            for (int i = 0; i < 20 ; i++) {
                dome.as();
            }
        },"线程一一").start();
        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                dome.sd();
            }
        },"线程二二").start();
    }
}
class Dome {
    int flag = 1;
    ReentrantLock reentrantLock = new ReentrantLock();
    Condition condition1 = reentrantLock.newCondition();
    Condition condition2 = reentrantLock.newCondition();
    public void as (){
        reentrantLock.lock();
        try {
            while (flag != 1){
                condition1.await();
            }
            System.out.println(Thread.currentThread().getName() + "aaaaa");
            flag = 2;
            condition2.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            reentrantLock.unlock();
        }
    }
    public void sd (){
        reentrantLock.lock();
        try {
            while (flag != 2){
                condition2.await();
            }
            System.out.println(Thread.currentThread().getName() + "cccccccccccccccc");
            flag = 1;
            condition1.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            reentrantLock.unlock();
        }
    }
}