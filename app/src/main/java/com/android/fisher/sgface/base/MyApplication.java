package com.android.fisher.sgface.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class MyApplication extends Application   {
    private static Context    mContext;
    private static Thread    mMainThread;
    private static long        mMainThreadId;
    private static Looper mMainLooper;
    private static Handler    mMainHandler;
    public static Context getContext()
    {
        return mContext;
    }
    public static Thread getMainThread()
    {
        return mMainThread;
    }
    public static long getMainThreadId()
    {
        return mMainThreadId;
    }
    public static Looper getMainThreadLooper()
    {
        return mMainLooper;
    }
    public static Handler getMainHandler()
    {
        return mMainHandler;
    }
    // 应用程序的入口
    @Override
    public void onCreate()
    {
        super.onCreate();
        // 上下文
        mContext = getApplicationContext();
        // 主线程
        mMainThread = Thread.currentThread();
        // mMainThreadId = mMainThread.getId();
        mMainThreadId = android.os.Process.myTid();
        mMainLooper = getMainLooper();
        // 创建主线程的handler
        mMainHandler = new Handler();

        //开始initokhttp
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS) //链接超时
            .readTimeout(300,TimeUnit.SECONDS) //读取超时
            .build(); //其他配置
        OkHttpUtils.initClient(okHttpClient);
    }
}
