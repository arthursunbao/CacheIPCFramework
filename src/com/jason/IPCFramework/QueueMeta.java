package com.jason.IPCFramework;

/**
 * Created by baosun on 8/8/2016.
 * Raw MetaData for Queue for storing the IPC Message
 */
public class QueueMeta {

    private final short groupId;
    private final int rawLength;
    private final long addr;
    private final byte storageType;

    public QueueMeta(short groupId, int rawLength, long addr, byte storageType) {
        this.groupId = groupId;
        this.rawLength = rawLength;
        this.addr = addr;
        this.storageType = storageType;
    }

    public byte getStorageType(){
        return storageType;
    }

    public long getAddrEnd(){
        return addr + rawLength;
    }

    public int getRawLength(){
        return rawLength;
    }

    public long getAddr(){
        return addr;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(addr^(addr >>> 32));
        result = prime * result + groupId;
        result = prime * result + rawLength;
        result = prime * result + storageType;
        return result;
    }

    @Override
    public boolean equal(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        QueueMeta other = (QueueMeta) obj;
        if(addr != other.addr){
            return false;
        }
        if(groupId != other.groupId){
            return false;
        }
        if(rawLength != other.rawLength){
            return false;
        }
        if(storageType != other.rawLength){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "QueueMeta [groupId=" + groupId + ", rawLenth=" + rawLength + ", addr=" + addr + ", storageType="
                + storageType + "]";
    }

}
