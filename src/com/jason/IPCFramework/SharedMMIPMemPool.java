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


    public SharedMMIPMemPool(final String loc, long len, boolean createNewFile) throws Exception{
        this.loc = loc;
        this.mm = UnsafeMemory.mapAndSetOffset(loc,len,createNewFile,0,len);
        init();
    }

    public String getLoc(){
        return loc;
    }

    private void init(){
        short curQueueCount = getQueueCountInMM();
        int addr = MM_QUEUE_START;
        SharedMMRing latest = null;
        long queueAddr = getFirstQueueAddr();
        for(int i = 0; i < curQueueCount; i++){
            short group = mm.getShortVolatile(addr);
            addr += 2;
            int rawLength = mm.getIntVolatile(addr);
            addr += 4;
            byte storageType = mm.getByteVolatile(addr);
            addr += 1;
            QueueMeta meta = new QueueMeta(group, rawLength, queueAddr, storageType);
            queueAddr += rawLength;
            SharedMMRing ring = new SharedMMRing(meta, mm.getAddr());
            allocateRings.put(group, ring);
            latest = ring;
        }
        this.lastRing = latest;
    }


    private long getFirstQueueAddr(){
        return MM_QUEUE_START + MM_QUEUE_METADATA_LEN * MAX_QUEUE_COUNT;
    }

    public synchronized SharedMMRing createNewRing(short groupId, int rawLength, byte storageType){
        short curQueueCount = getQueueCountInMM();
        if(curQueueCount >= MAX_QUEUE_COUNT){
            return null;
        }
        int metaAddr = MM_QUEUE_START + MM_QUEUE_METADATA_LEN * curQueueCount;
        mm.putShortVolatile(metaAddr, groupId);
        metaAddr += 2;
        mm.putIntVolatile(metaAddr, rawLength);
        metaAddr += 4;
        mm.putByteVolatile(metaAddr,storageType);
        SharedMMRing prevQueue = this.lastRing;
        long queueAddr = (prevQueue == null) ? getFirstQueueAddr() : prevQueue.getMetaData().getAddrEnd();
        QueueMeta meta = new QueueMeta(groupId, rawLength, queueAddr, storageType);
        SharedMMRing ring = new SharedMMRing(meta, mm.getAddr());
        mm.putShortVolatile(0, ++curQueueCount);
        allocateRings.put(groupId, ring);
        this.lastRing = ring;
        return ring;
    }

    public short getQueueCount(){
        return mm.getShortVolatile(0);
    }

    public SharedMMRing getRing(short i){
        return allocateRings.get(i);
    }

    public short getQueueCountInMM(){
        return mm.getShortVolatile(0);
    }

}
