package com.jason.IPCFramework;

/**
 * Created by baosun on 8/8/2016.
 */
public class util {
    public static long roundTo4096(long i) {
        return (i + 0xfffL) & ~0xfffL;
    }
    public static void  main(String[] args)
    {
        System.out.println(roundTo4096(4095));
    }
}
