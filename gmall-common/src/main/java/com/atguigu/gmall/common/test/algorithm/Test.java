package com.atguigu.gmall.common.test.algorithm;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        int[] arr = new int[] {5,9,3,7,2,45,12,7};
        test(arr,0,arr.length - 1);
        System.out.println(Arrays.toString(arr));

    }
    public static void test (int[] arr,int lief, int right){
        int l = lief;
        int r = right;
        int tem = 0;
        int povit = arr[( l + r) / 2];
        while (1< r){
            while (arr[l] < povit){
                l++;
            }
            while (arr[r] > povit){
                r--;
            }
            tem = arr[l];
            arr[l] = arr[r];
            arr[r] = tem;
            if (l >= r){
                break;
            }
            if (arr[l] == povit){
               r++;
            }
            if (arr[r] == povit){
                l--;
            }
        }
        if (l == r){
            l++;
            r--;
        }
        //左递归
        if (lief < r){
            test(arr,lief,r);
        }
        //右递归
        if (right > l){
            test(arr,l,right);
        }
    }
}
