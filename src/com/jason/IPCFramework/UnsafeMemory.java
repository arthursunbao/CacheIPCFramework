package com.jason.IPCFramework;

import sun.misc.Unsafe;
import sun.nio.ch.FileChannelImpl;
import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;


/**
 * Created by baosun on 8/8/2016.
 * This is the source class for using the Unsafe Memory in Java7
 */
public class UnsafeMemory {

    public static final Unsafe unsafe;
    public static final Method mmap;
    public static final Method unmmap;
    public static final int BYTE_ARRYAY_OFFSET;
    private final long addr;
    private final long startPos;
    private final long size;

    static{
        try{
            //Initialize the Unsafe Memory Region including unsafe, mmap, unmmap
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

    /**
     * Constructor for allocating a memory block for use
     * @param addr: Starter address for the memory allocation
     * @param startPos: Starting Position
     * @param size: Memory Block Size
     */

    public UnsafeMemory(long addr, long startPos, long size){

        this.addr = addr;
        this.startPos = startPos;
        this.size = size;

    }

    /**
     * Memory Allocation in the off-heap area
     * IPCFramework use RandomAccessFile, which is a memory-mapped file in Java IO Library.
     * It supports random write and read for large data comparing to InputStream and OutputStream class in Java IO.
     * @param loc: A String representing the location of the memory area
     * @param fileSize: File Size
     * @param createNewFile: Whether it is a newly created file
     * @param startPos: Starting position in the ringbuffer data structure
     * @param mapSize: Block Size for the file
     * @return UnsafeMemory Instance
     * @throws Exception
     */
    public static UnsafeMemory mapAndSetOffset(final String loc, long fileSize, boolean createNewFile, long startPos, long mapSize) throws Exception{
        long size = 0;
        RandomAccessFile backingFile = new RandomAccessFile(loc,"rw");
        if(createNewFile){
            new File(loc).delete();
            size = util.roundTo4096(fileSize);
            backingFile.setLength(size);
        }
        FileChannel ch = backingFile.getChannel();
        long addr = (long)mmap.invoke(ch,1,startPos,mapSize);
        ch.close();
        backingFile.close();
        return new UnsafeMemory(addr, startPos, mapSize);

    }

    /**
     * Get method from specific class, which is a reflection method
     * @param cls: Generic Class Name
     * @param name: Method Name
     * @param params: Method parameters
     * @return The function you want to have after reflection.
     *         In IPCFramework, it get the mmap and unmmap from Unsafe class
     * @throws Exception
     */

    private static Method getMethod(Class<?> cls, String name, Class<?>... params) throws Exception {
        Method m = cls.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }


    /**
     * Address Wrapper Class
     * @return addr
     */
    public long getAddr(){
        return addr;
    }

    /**
     * Return End Address
     * @return End Address
     */
    public long getEndAddr(){
        return addr + size;
    }

    /**
     * Get a relative position in a ring block given absolute position and starting position
     * @param abPos: absolute position
     * @return: relative position
     */
    public long getRelativePosition(long abPos){
        return abPos - startPos;
    }

    /**
     * Return starting position
     * @return
     */
    public long getStartPos(){
        return startPos;
    }

    /**
     * Return ending position
     * @return
     */
    public long getEndPos(){
        return getEndPos();
    }

    /**
     * return size of the block
     * @return
     */
    public long getSize(){
        return size;
    }

    /**
     * Wrapper class for ummap
     * @throws Exception
     */
    protected void ummap() throws Exception{
        unmmap.invoke(null, addr, this.size);
    }

    /**
     * Return the byte of the specified pos position
     * @param pos: Position of the byte
     * @return
     */
    public byte getByte(long pos){
        return unsafe.getByte(pos + addr);
    }

    /**
     * Volatile Version of Get Byte
     * @param pos
     * @return
     */
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

    public void putLong(long pos, long val){
        unsafe.putLong(pos + addr, val);
    }

    public void putLongVolatile(long pos, long val){
        unsafe.putLongVolatile(null, pos + addr, val);
    }


    /**
     * Direct Memory Copy from Object1 to Object2
     * @param pos: The Destination Address of the Object
     * @param data: Byte[] array Data
     * @param offset: Data starting position in the Byte[]
     * @param length: Length of the data
     */
    public void BytesCopy(long pos, byte[] data, int offset, int length){
        unsafe.copyMemory(null, pos +addr, data, BYTE_ARRYAY_OFFSET + offset, length);
    }

    /**
     * Copy bytes to specific Object
     * @param pos
     * @param data
     * @param offset
     * @param length
     */
    public void setBytes(long pos, byte[] data, int offset, int length){
        unsafe.copyMemory(data, BYTE_ARRYAY_OFFSET + offset, null, pos+addr, length );
    }

    /**
     * CAS Operation to compare and swap
     * @param pos: position to start looking
     * @param expected: expected value
     * @param value: value to be changed after the expected value is compare to be true;
     * @return
     */
    protected boolean compareAndSwapLong(long pos, long expected, long value) {
        return unsafe.compareAndSwapLong(null, pos + addr, expected, value);
    }

}
