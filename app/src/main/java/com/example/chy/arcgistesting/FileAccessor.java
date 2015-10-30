package com.example.chy.arcgistesting;

import android.os.Environment;

import java.io.File;

/**
 * Created by chy on 2015/9/8.
 */
public class FileAccessor {
    final static public String geoDatabasePath =getSDPath()  + "/test/test.geodatabase";
    final static public String tpkPath =getSDPath()  + "/test/test.tpk";
    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if   (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();

    }
}
