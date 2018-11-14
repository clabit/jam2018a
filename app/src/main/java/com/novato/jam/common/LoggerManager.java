package com.novato.jam.common;

import android.util.Log;

import com.novato.jam.BuildConfig;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class LoggerManager {
    static public void e(String tag, String msg){
        if(BuildConfig.DEBUG)Log.e(tag, msg);
    }
    static public void w(String tag, String msg){
        if(BuildConfig.DEBUG)Log.w(tag, msg);
    }
    static public void i(String tag, String msg){
        if(BuildConfig.DEBUG)Log.i(tag, msg);
    }
    static public void d(String tag, String msg){
        if(BuildConfig.DEBUG)Log.d(tag, msg);
    }
    static public void v(String tag, String msg){
        if(BuildConfig.DEBUG)Log.v(tag, msg);
    }
}
