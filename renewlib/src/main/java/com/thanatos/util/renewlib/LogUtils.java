package com.thanatos.util.renewlib;

import android.util.Log;

/**
 *  功能描述: 日志打印
 *  @className: LogUtils
 *  @author: thanatos
 *  @createTime: 2018/8/11
 *  @updateTime: 2018/8/11 14:22
 */
public class LogUtils {

    private static final String TAG = "ReNew";


    public static void d(String msg){
        if (BuildConfig.DEBUG){
            Log.d(TAG, msg);
        }
    }
}
