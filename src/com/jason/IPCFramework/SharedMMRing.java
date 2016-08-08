package com.jason.IPCFramework;

/**
 * Created by baosun on 8/8/2016.
 */
public class SharedMMRing {

    public static final byte STORAGE_PRIMARY = 0;
    public static final byte STORAGE_EXTEND = 1;
    public static final byte STORAGE_EXT_FILE = 2;

    public static final byte FLAG_NO_NEXT = 0B0001;
    public static final byte FLAG_NEXT_ADJACENT = 0B0010;
    public static final byte MASK_NEXT_REWIND = 0B0100;
    public static final byte MASK_NEXT_EXTEND_RING = 0B0101;
    public static final byte MASK_NEXT_BLOCK = 0B0110;
    public static final int MSG_PADDING_LENGTH = 2 + 1;

    private final UnsafeMemory mm;
    private final QueueMeta metaData;

    public final long getStartPos(){
        return 8 * 2;
    }

    public final long getWriteIndexPos(){
        return 8;
    }

    public final long getNextDataFlagIndexPos(){
        return 0;
    }

    public final long getEndPos(){
        return mm.getSize();
    }

    public SharedMMRing(QueueMeta metaData, long rawMemoryStartAddr ){
        super();
        this.mm = new UnsafeMemory(metaData.getAddr() + rawMemoryStartAddr, rawMemoryStartAddr, metaData.getRawLength());
        this.metaData = metaData;
        if(mm.compareAndSwapLong(getNextDataFlagIndexPos(),0,this.getStartPos())){
            mm.putLongVolatile(getWriteIndexPos(), this.getStartPos()+1);
            mm.putByte(getStartPos(),FLAG_NO_NEXT);
        }

    }

    private void writeMsg(long pos, byte[] data){
        short msgLength = (short) data.length;
        mm.putShort(pos,msgLength);
        mm.setBytes(pos + 2, data, 0, msgLength);
        mm.putByte(pos + 2 + msgLength, FLAG_NO_NEXT);
    }

    private int getMsgTotalSpace(byte[] msg){
        return msg.length;
    }

    public long getNextDataAddr(){
        return mm.getIntVolatile(0);
    }

    public long getWriteStartAddr(){
        return mm.getLongVolatile(8);
    }

    public UnsafeMemory getMm(){
        return mm;
    }

    public QueueMeta getMetaData(){
        return metaData;
    }

    private int tryReWindWrite(byte[] rawMsg) {
        int dataRealLen = getMsgTotalSpace(rawMsg);
        long writeStartPos = this.getWriteStartAddr();
        long nextDataPos = this.getNextDataAddr();

        if (writeStartPos + dataRealLen < nextDataPos) {
            if (!mm.compareAndSwapLong(getWriteIndexPos(), writeStartPos, writeStartPos + dataRealLen)) {
                return 1;
            }

            writeMsg(writeStartPos, rawMsg);
            mm.putByteVolatile(writeStartPos - 1, FLAG_NEXT_ADJACENT);
            return 0;
        }
        else if((writeStartPos > nextDataPos)){
            return 1;
        }
        else{
            return -1;
        }

    }

    private int tryWriteData(byte[] rawMsg){

        long nextDataBeginPos = this.getNextDataAddr();
        long writeStartPos = getWriteStartAddr();
        if(writeStartPos > nextDataBeginPos){
            int dataRealLen = getMsgTotalSpace(rawMsg);
            if(writeStartPos + dataRealLen <=this.getEndPos()){
                if(!mm.compareAndSwapLong(getWriteIndexPos(),writeStartPos,writeStartPos+dataRealLen)){
                    return 1;
                };
                this.writeMsg(writeStartPos,rawMsg);
                mm.putByteVolatile(writeStartPos - 1; FLAG_NEXT_ADJACENT);
                return 0;
            }
            else{
                if(mm.compareAndSwapLong(getWriteIndexPos(),writeStartPos, this.getStartPos()+1)){
                    mm.putByte(this.getStartPos(), FLAG_NO_NEXT);
                    mm.putByteVolatile(writeStartPos - 1, MASK_NEXT_REWIND);
                }
                return tryReWindWrite(rawMsg);

            }
        }
        else{
            return tryReWindWrite(rawMsg);
        }
    }

    public boolean putData(byte[] rawMsg){
        for(int i = 0; i < 3; i++){
            int writeResult = tryWriteData(rawMsg);
            switch (writeResult){
                case 0:
                    return true;
                case 1:
                    continue;
                case -1:
                    return false;
            }
        }
        return false;
    }

    private byte[] readData(long prevNextDataFlagPos, long curDataFlagPos){
        int dataLength = mm.getShort(curDataFlagPos + 1);
        long nextDataStartAddr = curDataFlagPos + MSG_PADDING_LENGTH;
        if(!mm.compareAndSwapLong(getNextDataFlagIndexPos(), prevNextDataFlagPos, nextDataStartAddr + dataLength)){
            return null;
        }
        byte[] msg = new byte[dataLength];
        mm.getBytes(nextDataStartAddr, msg, 0, dataLength);
        return msg;
    }

    public byte[] pullData(){
        byte[] msg = null;
        long nextDataFlagPos = this.getNextDataAddr();
        byte nextDataFlag = mm.getByteVolatile(nextDataFlagPos);
        switch(nextDataFlag){
            case FLAG_NO_NEXT:{
                break;
            }
            case FLAG_NEXT_ADJACENT:{
                msg = readData(nextDataFlagPos, nextDataFlagPos);
            }
            case FLAG_NEXT_REWIND:{
                byte newNextDataFlag = mm.getByteVolatile(this.getStartPos());
                switch(newNextDataFlag){
                    case FLAG_NO_NEXT:{
                        mm.compareAndSwapLong(getNextDataFlagIndexPos(), nextDataFlag,this.getStartPos())
                        break;
                    }
                    case FLAG_NEXT_ADJACENT:{
                        msg = readData(nextDataFlagPos, this.getStartPos());
                        break;
                    }
                }
            }
        }
        return msg;
    }





}
