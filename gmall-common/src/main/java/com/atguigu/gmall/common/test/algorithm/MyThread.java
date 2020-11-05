package com.atguigu.gmall.common.test.algorithm;

public class MyThread{
    static int num = 0;
    public static void main(String[] args) {
        new Thread(() -> {
            while (num <= 100){
                synchronized ("java"){
                    System.out.println(Thread.currentThread().getName() + num++);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    "java".notify();
                    try {
                        "java".wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        },"线程一").start();
        new Thread(() -> {
            while (num <= 100){
                synchronized ("java"){
                    System.out.println(Thread.currentThread().getName() + num++);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    "java".notify();
                    try {
                        "java".wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        },"线程二").start();
    }
}
