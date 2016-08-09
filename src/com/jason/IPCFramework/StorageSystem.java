package com.jason.IPCFramework;

import java.io.File;

/**
 * Created by baosun on 8/9/2016.
 */
public class StorageSystem {

    private static final int fileHeaderLen = 8 + 8;
    private final long mapBlockSize;
    private final String loc;
    private final boolean writeMode;
    private UnsafeMemory fileHaderMM;
    private volatile UnsafeMemory curMappingMM;
    private final long maxFileLen;
    public static final int MSG_PADDING_LENGTH = 2 + 1;

    public StorageSystem(final String loc, long maxLen, boolean writerMode, int mapBlockSize) throws Exception{
        if(writerMode){
            new File(loc).delete();
        }
        this.writeMode = writerMode;
        this.loc = loc;
        this.maxFileLen = util.roundTo4096(maxLen);
        this.mapBlockSize = util.roundTo4096(mapBlockSize);
        this.fileHaderMM = UnsafeMemory.mapAndSetOffset(loc, maxFileLen, writerMode, 0, fileHeaderLen);
        init();
    }

    public final long getNextDataFlagIndexPos(){
        return 0;
    }

    public final long getWriteIndexPos(){
        return 8;
    }

    public final long getStartPos(){
        return 8 * 2;
    }

    private void init() throws Exception{

        if(fileHaderMM.compareAndSwapLong(getNextDataFlagIndexPos(), 0, this.getStartPos())){
            fileHaderMM.putLongVolatile(getWriteIndexPos(), this.getStartPos() + 1);
            fileHaderMM.putByte(getStartPos(), SharedMMRing.FLAG_NO_NEXT);
        }
        if(writeMode){
            curMappingMM = UnsafeMemory.mapAndSetOffset(loc, maxFileLen, this.writeMode, 0, this.mapBlockSize);
        }
    }

    public void close() throws Exception{
        fileHaderMM.ummap();
        curMappingMM.ummap();
    }

    public int writeData(byte[] rawMsg) throws Exception{
        long writeStartPos = getWriteIndexPos();
        int dataRealLen = rawMsg.length;
        if(writeStartPos + dataRealLen < maxFileLen - fileHeaderLen){
            long endPos = writeStartPos + dataRealLen;
            UnsafeMemory mm = this.curMappingMM;
            long dataRelativePos = 0;
            if(mm.getEndPos() >= endPos){
                if(!fileHaderMM.compareAndSwapLong(getWriteIndexPos(), writeStartPos, endPos)){
                    return 0;
                }
                dataRelativePos = mm.getRelativePosition(writeStartPos);
                this.writeMsg(mm, dataRelativePos, rawMsg);
            }
            else{
                long nextWritePos = mm.getEndPos() + 1;
                if(fileHaderMM.compareAndSwapLong(getWriteIndexPos(),writeStartPos,nextWritePos)){
                    curMappingMM = UnsafeMemory.mapAndSetOffset(loc,maxFileLen,false,mm.getEndPos(),this.mapBlockSize);
                }
                UnsafeMemory nextmm = curMappingMM;
                nextmm.putByte(0,SharedMMRing.FLAG_NO_NEXT);
                mm.putByteVolatile(dataRelativePos - 1, SharedMMRing.MASK_NEXT_BLOCK);
                mm.ummap();
                mm = curMappingMM;
                while(mm.getStartPos() < writeStartPos){
                    Thread.yield();;
                    mm = curMappingMM;
                }
                dataRelativePos = mm.getRelativePosition(nextWritePos);
                this.writeMsg(mm,dataRelativePos,rawMsg);
            }
            mm.putByteVolatile(dataRelativePos - 1, SharedMMRing.FLAG_NEXT_ADJACENT);
            return 1;
        } else{
            return -1;
        }
    }

    private void writeMsg(UnsafeMemory mm, long pos, byte[] data) {

        short msgLength = (short) data.length;
        mm.putShort(pos, msgLength);
        mm.setBytes(pos + 2, data, 0, msgLength);
        mm.putByte(pos + 2 + msgLength, SharedMMRing.FLAG_NO_NEXT);

    }

    public long getNextDataAddr() {
        return fileHaderMM.getLongVolatile(0);

    }

    public long getWriteStartAddr() {
        return fileHaderMM.getLongVolatile(8);

    }

    private int getMsgTotalSpace(byte[] msg) {
        return msg.length + MSG_PADDING_LENGTH;
    }

}
