package com.atguigu.gmall.common.test.algorithm;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadTest {
    public static void main(String[] args) {
        DomeTest dome = new DomeTest();
        new Thread(() -> {
            dome.as();
        }, "奇数线程").start();
        new Thread(() -> {
            dome.ds();
        }, "偶数线程").start();
    }
}
class DomeTest {
    int flag = 1;
    int m = 100;
    private final Lock reentrantLock = new ReentrantLock();
    private  final Condition condition1 = reentrantLock.newCondition();
    private  final Condition condition2 = reentrantLock.newCondition();
    public void as (){

        for (int i = 0; i <50 ; i++) {
            reentrantLock.lock();
            try {
                while (flag != 1){
                    condition1.await();
                }
                System.out.println(Thread.currentThread().getName() + "----->" + m--);
                flag = 2;
                condition2.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
            }

        }


    }
    public void ds(){

        for (int i = 0; i < 50; i++) {
            reentrantLock.lock();
            try {
                while (flag != 2){
                    condition2.await();
                }
                System.out.println(Thread.currentThread().getName() + "----->" + m--);
                flag = 1;
                condition1.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
            }

        }

    }
}
