package com.atguigu.gmall.common.test.lock;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaPhoreDemo {
    public static void main(String[] args) {
        //初始化信号量,3 个车位
        Semaphore semaphore = new Semaphore(3);
        //6 个线程, 模拟6 辆车
        for (int i = 0; i < 6; i++) {
            new Thread(() -> {
                try {
                    //  抢占一个车位
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName() + " 抢到了一个停车位！");
                    //停一会开走了
                    TimeUnit.SECONDS.sleep(new Random().nextInt(3));
                    System.out.println(Thread.currentThread().getName() + " 离开停车位！");
                    //离开停车位 释放停车位
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }
    }
}
