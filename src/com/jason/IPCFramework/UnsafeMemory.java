package com.jason.IPCFramework;

import com.sun.deploy.util.SyncFileAccess;
import com.sun.java.util.jar.pack.Package;
import sun.misc.Unsafe;
import sun.nio.ch.FileChannelImpl;
import sun.nio.ch.Util;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

import static com.sun.org.glassfish.gmbal.ManagedObjectManagerFactory.getMethod;

/**
 * Created by baosun on 8/8/2016.
 */
public class UnsafeMemory {
    public static final Unsafe unsafe;
    public static final Method mmap;
    public static final Method unmmap;
    public static final int BYTE_ARRYAY_OFFSET;

    private long addr, startPos, size;

    static{
        try{
            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            unsafe = (Unsafe)singleoneInstanceField.get(null);
            mmap = getMethod(FileChannelImpl.class, "map0", int.class, long.class, long.class);
            mmap.setAccessible(true);
            unmmap = getMethod(FileChannelImpl.class, "unmap0", int.class,long.class,long.class);
            unmmap.setAccessible(true);
            BYTE_ARRYAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);
        }
        catch(Exception e){
            throw new RuntimeException();
        }
    }

    public UnsafeMemory(long addr, long startPos, long size){

        this.addr = addr;
        this.startPos = startPos;
        this.size = size;

    }

    public static UnsafeMemory mapAndSetOffset(final String loc, long fileSize, boolean createNewFile, long startPos, long mapSize) throws Exception{
        long size = 0;
        RandomAccessFile backingFile = new RandomAccessFile(loc,"rw");
        if(createNewFile){
            new File(loc).delete();
            size = Util.roundTo4096(fileSize);
            backingFile.setLength(size);
        }
        FileChannel ch = backingFile.getChannel();
        long addr = (long)mmap.invoke(ch,1,startPos,mapSize);
        ch.close();
        backingFile.close();
        return new UnsafeMemory(addr, startPos, mapSize);

    }


    private static Method getMethod(Class<?> cls, String name, Class<?>... params) throws Exception {
        Method m = cls.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    public long getAddr(){
        return addr;
    }

    public long getEndAddr(){
        return addr + size;
    }

    public long getRelovePos(long abPos){
        return abPos - startPos;
    }

    public long getStartPos(){
        return startPos;
    }

    public long getEndPos(){
        return getEndPos();
    }

    public long getSize(){
        return size;
    }

    protected void ummap() throws Exception{
        unmmap.invoke(null, addr, this.size);

    }

    public byte getByte(long pos){
        return unsafe.getByte(pos + addr);
    }

    protected byte getByteVolatile(long pos){
        return unsafe.getByteVolatile(null, pos + addr);
    }

    public short getShort(long pos){
        return unsafe.getShort(pos + addr);
    }

    protected short getShortVolatile(long pos){
        return unsafe.getShortVolatile(null, pos + addr);
    }

    public int getInt(int pos){
        return unsafe.getInt(pos + addr);
    }

    protected int getIntVolatile(long pos){
        return unsafe.getIntVolatile(null, pos + addr);
    }

    public Long getLong(long pos){
        return unsafe.getLong(pos + addr);
    }

    protected Long getLongVolatile(long pos){
        return unsafe.getLongVolatile(null, pos + addr);
    }

    public void putByte(long pos, byte val){
        unsafe.putByte(pos + addr, val);
    }

    protected void putByteVolatile(long pos, byte val){
        unsafe.putByteVolatile(null, pos + addr, val);
    }

    public void putInt(long pos, int val){
        unsafe.putInt(pos + addr, val);
    }

    protected void putIntVolatile(long pos, int val){
        unsafe.putIntVolatile(null, pos + addr, val);
    }

    public void putShort(long pos, short val){
        unsafe.putShort(pos + addr, val);
    }

    protected void putShortVolatile(long pos, short val){
        unsafe.putShortVolatile(null, pos + addr, val);
    }

    public void putShort(long pos, long val){
        unsafe.putLong(pos + addr, val);
    }

    public void putShortVolatile(long pos, long val){
        unsafe.putLongVolatile(null, pos + addr, val);
    }

    public void getBytes(long pos, byte[] data, int offset, int length){
        unsafe.copyMemory(null, pos +addr, data, BYTE_ARRYAY_OFFSET + offset, length);
    }

    public void setBytes(long pos, byte[] data, int offset, int length){
        unsafe.copyMemory(data, BYTE_ARRYAY_OFFSET + offset, null, pos+addr, length );
    }

    protected boolean compareAndSwapLong(long pos, long expected, long value) {
        return unsafe.compareAndSwapLong(null, pos + addr, expected, value);
    }

    protected long getAndAddLong(long pos, long delta) {
        return unsafe.getAndAddLong(null, pos + addr, delta);
    }

}
