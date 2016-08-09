package test.java;

import com.jason.IPCFramework.SharedMMIPMemPool;
import com.jason.IPCFramework.SharedMMRing;

/**
 * Created by baosun on 8/9/2016.
 */
public class Writer {
    public static void writerTest(){
        int i = 0;
        long start = System.currentTimeMillis();
        try{
            SharedMMIPMemPool pool = new SharedMMIPMemPool("D:\simpleTest.txt", 1024 * 1024 * 100L, false);
            SharedMMRing ring = pool.getRing((short)1);

            while(true){
                ++i;
                byte[] data = ("hello  " + (i++)).getBytes("utf-8");
                while(!ring.putData(data)){
                    Thread.yield();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

}
