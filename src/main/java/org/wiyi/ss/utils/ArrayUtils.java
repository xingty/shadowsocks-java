package org.wiyi.ss.utils;

public class ArrayUtils {
    public static byte[] merge(byte[] arr1, byte[] arr2) {
        byte[] merge = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1,0,merge,0,arr1.length);
        System.arraycopy(arr2,0,merge,arr1.length,arr2.length);

        return merge;
    }

    public static boolean isEmpty(byte[] arr) {
        return arr == null || arr.length == 0;
    }
}
