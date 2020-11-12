package com.atguigu.gmall.common.test.lock;


import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ContDowLatchDemo {
    //main方法也是一个进程，在这里是主进程，即锁门的同学
    public static void main(String[] args) throws InterruptedException {
        //初始化计数器,初始值是六
        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 0; i < 6; i++) {
            new Thread(() -> {
                try {
                    //每个同学墨迹三秒
                    TimeUnit.SECONDS.sleep(new Random().nextInt(3));
                    System.out.println(Thread.currentThread().getName() + " 出来了");
                    //调用conutDown()计数器 减1
                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        //调用计数器的await()方法,等待6个人出来
       // countDownLatch.await();
        System.out.println("索美啦");
    }
}
