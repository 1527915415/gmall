package com.atguigu.gmall.common.test.algorithm;

import java.util.Arrays;

public class dome3 {
    public static void main(String[] args) {
        int[] arr = new int[]{-32,-78,0,33,0,-86,0,70,6};
        quickSort(arr,0,arr.length - 1);
        System.out.println(Arrays.toString(arr));
    }
    public static void quickSort(int[] arr ,int left,int right){
        int l = left ;//左下标
        int r = right; //右下标
        int pivot = arr[(l + r) / 2];//中间值
        int temp = 0;
        //
        while (l < r){
            //在左边一直找,找到大于pivot的值才推出
            while ( arr[l] < pivot){
                l++;
            }
            //在左右一直找,找到小于pivot的值才推出
            while ( arr[r] > pivot){
                r--;
            }
            if (l >= r ){
                break;
            }
            //交换值
            temp = arr[l];
            arr[l] = arr[r];
            arr[r] = temp;
            //
            if (arr[l] == pivot){
                r--;
            }
            if (arr[r] == pivot){
                l++;
            }
        }
        if (l ==r){
            l++;
            r--;
        }
        //向左递归
        if (left < r){
            quickSort(arr,left,r);
        }
        //向右递归
        if (right > l){
            quickSort(arr,l,right);
        }
    }
}
