package com.jason.IPCFramework;

/**
 * Created by baosun on 8/8/2016.
 */
public class RawMessage {

    private short msgLength;
    private byte[] rawData;
    private byte[] dataFlag;

    public RawMessage{

    }

    public byte[] getRawData(){
        return rawData;
    }

    public byte[] dataFlag(){
        return dataFlag;
    }

    public RawMessage(byte[] rawData){
        super();
        this.rawData = rawData;
        this.msgLength = (short)rawData.length;
    }
}
