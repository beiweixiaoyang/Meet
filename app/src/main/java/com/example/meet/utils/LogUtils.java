package com.example.meet.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * 日志工具类
 */
public class LogUtils {

    private static final String TAG="Meet";
    public static void i(String text){
        if(!TextUtils.isEmpty(text)){
            Log.i(TAG,":"+text);
        }
    }
    public static void d(String text){
        if(!TextUtils.isEmpty(text)){
            Log.d(TAG,":"+text);
        }
    }
    public static void e(String text){
        if(!TextUtils.isEmpty(text)){
            Log.e(TAG,":"+text);

        }
    }
}
