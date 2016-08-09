package com.jason.IPCFramework;

/**
 * Created by baosun on 8/8/2016.
 */
public class util {
    /**
     * Take approximate value of a certain long to make it memory aligned
     * @param i
     * @return
     */
    public static long roundTo4096(long i) {
        return (i + 0xfffL) & ~0xfffL;
    }

}
