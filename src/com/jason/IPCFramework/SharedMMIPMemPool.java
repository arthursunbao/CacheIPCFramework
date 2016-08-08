package com.jason.IPCFramework;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by baosun on 8/8/2016.
 */
public class SharedMMIPMemPool {

    private final String loc;
    private UnsafeMemory mm;
    private final int MAX_QUEUE_COUNT = 2048;
    private final int MM_QUEUE_START = 2;
    private final int MM_QUEUE_METADATA_LEN = 2 + 4 + 1;
    private ConcurrentHashMap<Short, SharedMMRing> allocateRings = new ConcurrentHashMap<Short, SharedMMRing>();
    private volatile SharedMMRing lastRing;
}
