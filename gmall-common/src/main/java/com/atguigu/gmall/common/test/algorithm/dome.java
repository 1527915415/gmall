package com.atguigu.gmall.common.test.algorithm;


public class dome {
    public static void main(String[] args) {
       int[] num = new int[]{1,8,2,7,3,6,5,4};
        for (int i = 0; i < num.length -1 ; i++) {
            for (int j = 0; j < num.length - 1 - i ; j++) {
                if (num[j] >num[j +1]) {
                    int temp = num[j];
                    num[j] = num[j +1];
                    num[j + 1]=temp;
                }
            }
        }
        for (int i = 0; i < num.length; i++) {
            System.out.println(num[i]);
        }
    }
}
