package test.java;

import com.jason.IPCFramework.SharedMMIPMemPool;
import com.jason.IPCFramework.SharedMMRing;

/**
 * Created by baosun on 8/9/2016.
 */
public class Reader {

    public void readerTest(){
        try{
            SharedMMIPMemPool pool = new SharedMMIPMemPool("D:\\simpleTest.txt", 1024 * 1024 * 1024L, true);
            SharedMMRing ring = pool.createNewRing((short) 1, 1024 * 1024, SharedMMRing.STORAGE_PRIMARY);
            long alreadyRead = 0;
            long currentTime = System.currentTimeMillis();
            while(true){
                byte[] data = ring.pullData();
                if(data != null){
                    ++alreadyRead;
                    if(alreadyRead % 1000000 == 0){
                        String finalData = new String(data);
                        System.out.println("Already Read Data " + alreadyRead + ", speed " + alreadyRead * 1000L / (System.currentTimeMillis() - currentTime));
                    }
                }
                else{
                    Thread.yield();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
